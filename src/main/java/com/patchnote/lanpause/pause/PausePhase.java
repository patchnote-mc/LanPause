package com.patchnote.lanpause.pause;

public enum PausePhase
{
    /** Game running normally, no vote in progress. */
    RUNNING,
    /** Game frozen, initial timed vote on whether to stay paused. */
    VOTING,
    /** Game frozen and committed; an untimed unpause vote is showing. */
    PAUSED
}
