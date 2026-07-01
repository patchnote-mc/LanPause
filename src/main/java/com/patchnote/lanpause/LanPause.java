package com.patchnote.lanpause;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LanPause
{
    public static final String MOD_ID = "lanpause";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    private LanPause() {}
}
