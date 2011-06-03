This Open Source mod aims to implement a sorting feature for inventories in [Minecraft][1], a game by [Mojang AB][2]. Since everyone has his own habits on how to organize items, this mods tries to be as customizable as possible, without being annoying to set up.

For documentation about how to use or install the mod, see the [main page of Inventory Tweaks][3]. This place is for developers!

# Dependencies

This repository doesn't include any third party code, so in order to compile the current version of this mod, the following is needed:

 * Minecraft's `minecraft.jar` (version *Beta 1.6.5*)
 * [ModLoader][4] (version *Beta 1.6.5*)
 * [Minecraft Coder Pack][5] (version *3.3*)

# Compiling the mod

Pull this repository from the `src/minecraft/net/minecraft/` folder of MCP, it should then be ready for compilation. You will also have to copy `DefaultConfig.text` and `DefaultTree.txt` (found in `src/`) to the `bin/minecraft/net/minecraft/src` folder, or else the mod will fail to generate the configuration files.

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