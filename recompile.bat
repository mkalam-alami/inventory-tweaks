@echo off
runtime\bin\python\python_mcp runtime\recompile.py %*
runtime\bin\python\python_mcp invtweaks_scripts\copy_resources.py
pause
