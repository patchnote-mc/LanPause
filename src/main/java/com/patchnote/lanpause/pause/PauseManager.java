package com.patchnote.lanpause.pause;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.patchnote.lanpause.config.ModConfig;
import com.patchnote.lanpause.net.LanPauseNet;
import com.patchnote.lanpause.net.LanPauseNet.FreezeSyncPayload;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.ConfirmationDialog;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side vote-driven pause state machine. One instance is bound to the running
 * {@link MinecraftServer}. All mutating calls happen on the server thread (networking receivers,
 * the vote mixin, tick and lifecycle events all run there).
 */
public final class PauseManager
{
    private static PauseManager instance;

    public static void bind(MinecraftServer server) { instance = new PauseManager(server); }

    public static void unbind()
    {
        if (instance != null) instance.hardReset();
        instance = null;
    }

    public static PauseManager get() { return instance; }

    private final MinecraftServer server;
    private PausePhase phase = PausePhase.RUNNING;
    private final Map<UUID, String> votes = new HashMap<>();
    private String initiatorName = "";
    private int ticksRemaining = 0;

    private PauseManager(MinecraftServer server) { this.server = server; }

    // ---- entry points -------------------------------------------------------

    /** A player pressed the pause keybind. */
    public void requestPause(ServerPlayer initiator)
    {
        if (phase != PausePhase.RUNNING)
        {
            // Vote already running / paused: just (re)show the current dialog to this player.
            openTo(initiator, currentDialog());
            return;
        }

        ModConfig cfg = ModConfig.get();
        phase = PausePhase.VOTING;
        votes.clear();
        initiatorName = initiator.getName().getString();
        if (cfg.initiatorAutoVotes) votes.put(initiator.getUUID(), LanPauseNet.CHOICE_PAUSE);
        ticksRemaining = computeVoteTicks(cfg, onlineCount());

        setFrozen(true);
        if (!evaluate()) broadcastDialog();
    }

    /** A vote button was clicked (routed here from the custom-click-action mixin). */
    public void onVote(ServerPlayer voter, String choice)
    {
        if (phase == PausePhase.RUNNING) return;
        if (!LanPauseNet.CHOICE_PAUSE.equals(choice) && !LanPauseNet.CHOICE_RESUME.equals(choice)) return;

        votes.put(voter.getUUID(), choice);
        if (!evaluate()) broadcastDialog();
    }

    /** Server tick: only the initial vote is timed. */
    public void tick()
    {
        if (phase != PausePhase.VOTING) return;
        if (--ticksRemaining <= 0)
        {
            resolveByMajority();
            return;
        }
        if (ticksRemaining % 20 == 0) broadcastDialog(); // refresh countdown once per second
    }

    public void onPlayerJoin(ServerPlayer player)
    {
        if (phase == PausePhase.RUNNING) return;
        ServerPlayNetworking.send(player, new FreezeSyncPayload(true));
        openTo(player, currentDialog());
        if (!evaluate()) broadcastDialog(); // more players -> threshold changed
    }

    public void onPlayerLeave(ServerPlayer player)
    {
        if (phase == PausePhase.RUNNING) return;
        boolean removed = votes.remove(player.getUUID()) != null;
        if (!evaluate() && removed) broadcastDialog(); // fewer players -> threshold may now be met
    }

    // ---- resolution ---------------------------------------------------------

    /**
     * @return true if this caused a phase transition (which already refreshed dialogs / freeze).
     */
    private boolean evaluate()
    {
        int online = onlineCount();
        int needed = neededVotes(online);

        if (phase == PausePhase.VOTING)
        {
            if (count(LanPauseNet.CHOICE_PAUSE) >= needed) { commitPaused(); return true; }
            if (online > 0 && votes.size() >= online) { resolveByMajority(); return true; }
        }
        else if (phase == PausePhase.PAUSED)
        {
            if (count(LanPauseNet.CHOICE_RESUME) >= needed) { resume(); return true; }
        }
        return false;
    }

    private void resolveByMajority()
    {
        int pause = count(LanPauseNet.CHOICE_PAUSE);
        int resume = count(LanPauseNet.CHOICE_RESUME);
        if (pause > resume) commitPaused();
        else if (resume > pause) resume();
        else if (ModConfig.get().onTimerTie == ModConfig.TieBreak.PAUSE) commitPaused();
        else resume();
    }

    private void commitPaused()
    {
        phase = PausePhase.PAUSED;
        votes.clear();
        if (!LanPauseFreeze.active) setFrozen(true);
        broadcastDialog();
    }

    private void resume()
    {
        phase = PausePhase.RUNNING;
        votes.clear();
        setFrozen(false);
        clearDialogs();
    }

    private void hardReset()
    {
        phase = PausePhase.RUNNING;
        votes.clear();
        LanPauseFreeze.active = false;
    }

    // ---- freeze + helpers ---------------------------------------------------

    private void setFrozen(boolean frozen)
    {
        LanPauseFreeze.active = frozen;
        server.tickRateManager().setFrozen(frozen);
        for (ServerPlayer p : players()) ServerPlayNetworking.send(p, new FreezeSyncPayload(frozen));
    }

    private List<ServerPlayer> players() { return server.getPlayerList().getPlayers(); }

    private int onlineCount() { return players().size(); }

    private int neededVotes(int online)
    {
        return Math.max(1, (int) Math.ceil(online * ModConfig.get().voteThreshold));
    }

    private int count(String choice)
    {
        int n = 0;
        for (String c : votes.values()) if (choice.equals(c)) n++;
        return n;
    }

    private static int computeVoteTicks(ModConfig cfg, int online)
    {
        int secs = cfg.minVoteSeconds + Math.max(0, online - 1) * cfg.extraSecondsPerPlayer;
        secs = Math.max(cfg.minVoteSeconds, Math.min(cfg.maxVoteSeconds, secs));
        return secs * 20;
    }

    // ---- dialog broadcast ---------------------------------------------------

    private void broadcastDialog()
    {
        Dialog dialog = currentDialog();
        for (ServerPlayer p : players()) openTo(p, dialog);
    }

    private void clearDialogs()
    {
        for (ServerPlayer p : players()) p.connection.send(ClientboundClearDialogPacket.INSTANCE);
    }

    private void openTo(ServerPlayer player, Dialog dialog)
    {
        player.openDialog(Holder.direct(dialog));
    }

    private Dialog currentDialog()
    {
        return phase == PausePhase.PAUSED ? pausedDialog() : voteDialog();
    }

    private Dialog voteDialog()
    {
        int secondsLeft = (ticksRemaining + 19) / 20;
        Component prompt = Component.literal(initiatorName + " voted to pause the game. Vote your decision:");
        Component counts = Component.literal(
            "Votes  —  Pause: " + count(LanPauseNet.CHOICE_PAUSE)
                + "   Don't Pause: " + count(LanPauseNet.CHOICE_RESUME)
                + "   (" + secondsLeft + "s left)");

        ActionButton pauseBtn = voteButton("Pause", LanPauseNet.CHOICE_PAUSE);
        ActionButton resumeBtn = voteButton("Don't Pause", LanPauseNet.CHOICE_RESUME);
        return new ConfirmationDialog(commonData(Component.literal("LAN Pause"), prompt, counts), pauseBtn, resumeBtn);
    }

    private Dialog pausedDialog()
    {
        Component prompt = Component.literal("The game is paused. Vote to unpause:");
        Component counts = Component.literal(
            "Votes  —  Unpause: " + count(LanPauseNet.CHOICE_RESUME)
                + "   Keep Paused: " + count(LanPauseNet.CHOICE_PAUSE));

        ActionButton unpauseBtn = voteButton("Unpause", LanPauseNet.CHOICE_RESUME);
        ActionButton keepBtn = voteButton("Keep Paused", LanPauseNet.CHOICE_PAUSE);
        return new ConfirmationDialog(commonData(Component.literal("Game Paused"), prompt, counts), unpauseBtn, keepBtn);
    }

    private static CommonDialogData commonData(Component title, Component prompt, Component counts)
    {
        List<DialogBody> body = List.of(
            new PlainMessage(prompt, PlainMessage.DEFAULT_WIDTH),
            new PlainMessage(counts, PlainMessage.DEFAULT_WIDTH));
        return new CommonDialogData(
            title,
            Optional.empty(),
            false,               // canCloseWithEscape: lock players to the dialog
            false,               // pause: we run our own tick-freeze, not the dialog's
            DialogAction.NONE,   // keep the dialog open after a vote so counts can refresh
            body,
            List.of());
    }

    private static ActionButton voteButton(String label, String choice)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString(LanPauseNet.CHOICE_KEY, choice);
        Action action = new CustomAll(LanPauseNet.VOTE_ID, Optional.of(tag));
        return new ActionButton(new CommonButtonData(Component.literal(label), CommonButtonData.DEFAULT_WIDTH),
            Optional.of(action));
    }
}
