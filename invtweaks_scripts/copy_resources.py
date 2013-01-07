# -*- coding: utf-8 -*-

import shutil
import sys
import os

# Functions

INVTWEAKS_SRC = 'src/minecraft/invtweaks/'
INVTWEAKS_BIN = 'bin/minecraft/invtweaks/'

def copy_resource_tree(resource):
    shutil.rmtree(INVTWEAKS_BIN + resource, True)
    shutil.copytree(INVTWEAKS_SRC + resource, INVTWEAKS_BIN + resource)
    
def copy_resource(resource):
    try:
        os.remove(INVTWEAKS_BIN + resource)
    except OSError:
        pass
    shutil.copy(INVTWEAKS_SRC + resource, INVTWEAKS_BIN + resource)

# Resources copy

sys.stdout.write('Copying InvTweaks resources...\n')

copy_resource_tree('lang/')
copy_resource('DefaultConfig.dat')
copy_resource('DefaultTree.dat')

sys.stdout.write('OK!\n')