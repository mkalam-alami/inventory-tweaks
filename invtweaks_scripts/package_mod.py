# -*- coding: utf-8 -*-

import shutil
import sys
import os
import zipfile

# Constants

INVTWEAKS_SRC = 'src/minecraft/invtweaks'
INVTWEAKS_CLASS = 'reobf/minecraft'
INVTWEAKS_TMP = 'temp/invtweaks'

# Remove tmp dir

shutil.rmtree(INVTWEAKS_TMP, True)

# Gather files

print 'Packaging InvTweaks...'

shutil.copytree(INVTWEAKS_CLASS, INVTWEAKS_TMP)
shutil.copy(INVTWEAKS_SRC + '/DefaultConfig.dat', INVTWEAKS_TMP + '/invtweaks/DefaultConfig.dat')
shutil.copy(INVTWEAKS_SRC + '/DefaultTree.dat', INVTWEAKS_TMP + '/invtweaks/DefaultTree.dat')
shutil.copytree(INVTWEAKS_SRC + '/lang', INVTWEAKS_TMP + '/invtweaks/lang')
shutil.copy('INVTWEAKS-LICENSE.md', INVTWEAKS_TMP + '/INVTWEAKS-LICENSE.txt')

# Zip

def get_compression_mode():
    try:
        import zlib
        return zipfile.ZIP_DEFLATED
    except:
        print '(...but without compressing)'
        return zipfile.ZIP_STORED

def write_to_zip(zip, compression, path):
    archive_path = path.replace(INVTWEAKS_TMP, '')
    zip.write(path, arcname=archive_path, compress_type=compression)

def list_files_recursive(folder):
    contents = []
    for folder_element in os.listdir(folder):
        sys.stdout.write(folder_element + '\n')
        if os.path.isfile(folder_element):
            contents.append(folder_element)
        else:
            contents.extend(list_files_recursive(folder_element))
    return contents

print 'Zipping InvTweaks...'

compression_mode = get_compression_mode()
invtweaks_zip = zipfile.ZipFile('InvTweaks-DEV.zip', mode='w')
for root, dirs, files in os.walk(INVTWEAKS_TMP):
    for file in files:
        write_to_zip(invtweaks_zip, compression_mode, root + '/' + file)

print 'OK!'