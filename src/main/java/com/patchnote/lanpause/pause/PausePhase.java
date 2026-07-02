package com.patchnote.lanpause.pause;

public enum PausePhase
{
    /** Game running normally, no vote in progress. */
    RUNNING,
    /** Game frozen, a timed vote in progress — to pause (from RUNNING) or to resume (from PAUSED). */
    VOTING,
    /** Game frozen and committed; the idle paused dialog offers a "Start Resume Vote" button. */
    PAUSED
}
