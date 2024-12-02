package edu.Kennesaw.ksumcspeedrun.Utilities;

import org.bukkit.generator.ChunkGenerator;

/**
 * World Generator Class for Speedrun Map Generation - All settings are default; no real use for this class yet.
 *  TODO - ADD ADDITIONAL WORLD CUSTOMIZATION IN config.yml:
 *       - Custom World Generator Settings
 *       - Custom Biomes
 *       - Specific tweaks that cannot be adjusted otherwise: ore spawn limit, custom structures, etc.
 */
public class WorldGenerator extends ChunkGenerator {

    @Override
    public boolean shouldGenerateSurface() {
        return true;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return true;
    }

}