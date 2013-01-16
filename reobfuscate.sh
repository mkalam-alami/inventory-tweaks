#!/bin/bash
python runtime/reobfuscate.py "$@"
python invtweaks_scripts/package_mod.py
