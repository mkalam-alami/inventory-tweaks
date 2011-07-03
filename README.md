This Open Source mod aims to implement a sorting feature for inventories in [Minecraft][1], a game by [Mojang AB][2]. Since everyone has his own habits on how to organize items, this mods tries to be as customizable as possible, without being annoying to set up.

For documentation about how to use or install the mod, see the [main page of Inventory Tweaks][3]. This place is for developers!

# Dependencies

This repository doesn't include any third party code, so in order to compile the current version of this mod, the following is needed:

 * Minecraft's `minecraft.jar` (version *Beta 1.7.2*)
 * [ModLoader][4] (version *Beta 1.7.2*)

The mod doesn't use MCP anymore, to allow for faster updates when a new version of Minecraft comes out.

# Compiling the mod

After building the sources, you can either move them directly to the `minecraft.jar` file, or ZIP them the same way the mod is packaged, then put the archive in the `mods` folder. Don't forget to copy `DefaultConfig.text` and `DefaultTree.txt` (found in `src/`) with the files, or else the mod will fail to generate the configuration files.

I'll soon make an Ant task to do the install more easily.

# Developing

 * You can enable verbose logging by adding a "DEBUG" rule in *InvTweaksConfig.txt*.
 * The sorting algorithm is probably not very easy to understand, but if you're interested in having some documentation about how it works, please contact me, and I'll write something on this repository's wiki. Meanwhile, there is already a few comments to help.

# Contribute

You're welcome to fork this repository ; do whatever you want with it as long as you respect the license requirements and the [Minecraft's terms][6]. If you're looking for how to help, why not check the issue list?

[1]: http://www.minecraft.net/
[2]: http://mojang.com/
[3]: http://wan.ka.free.fr/?invtweaks
[4]: http://www.minecraftforum.net/viewtopic.php?t=80246
[5]: http://mcp.ocean-labs.de/index.php/MCP_Releases
[6]: http://www.minecraft.net/copyright.jsp