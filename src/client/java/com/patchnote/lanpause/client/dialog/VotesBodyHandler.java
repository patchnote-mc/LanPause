package com.patchnote.lanpause.client.dialog;

import java.util.List;

import com.patchnote.lanpause.dialog.VotesBody;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ItemDisplayWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.gui.screens.dialog.body.DialogBodyHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Renders {@link VotesBody} as a two-column grid: a header per column with a row of player heads
 * beneath each. Heads show the item tooltip on hover (the item's custom name = the player's name).
 */
public class VotesBodyHandler implements DialogBodyHandler<VotesBody>
{
    @Override
    public LayoutElement createControls(DialogScreen<?> parent, VotesBody body)
    {
        Font font = parent.getFont();
        GridLayout grid = new GridLayout();
        grid.columnSpacing(24).rowSpacing(4);
        grid.defaultCellSetting().alignHorizontallyCenter();

        grid.addChild(new StringWidget(body.leftHeader(), font), 0, 0);
        grid.addChild(new StringWidget(body.rightHeader(), font), 0, 1);
        grid.addChild(headRow(body.leftHeads(), body.cellSize(), font), 1, 0);
        grid.addChild(headRow(body.rightHeads(), body.cellSize(), font), 1, 1);
        return grid;
    }

    private static LayoutElement headRow(List<ItemStackTemplate> heads, int size, Font font)
    {
        LinearLayout row = LinearLayout.horizontal().spacing(2);
        row.defaultCellSetting().alignVerticallyMiddle();
        if (heads.isEmpty())
        {
            row.addChild(new StringWidget(Component.literal("—"), font));
            return row;
        }
        Minecraft mc = Minecraft.getInstance();
        for (ItemStackTemplate template : heads)
        {
            ItemStack stack = template.create();
            row.addChild(new ItemDisplayWidget(mc, 0, 0, size, size, stack.getHoverName(), stack, false, true));
        }
        return row;
    }
}
