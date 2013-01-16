@echo off
runtime\bin\python\python_mcp runtime\reobfuscate.py %*
runtime\bin\python\python_mcp invtweaks_scripts\package_mod.py
pause
