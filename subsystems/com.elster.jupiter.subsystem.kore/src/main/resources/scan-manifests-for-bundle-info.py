#!/usr/bin/python
"""
Generates list of bundle symbolic names and bundle version from the manifest files
* scans current directory for .jar files
* extracts manifest.mf
* gets Bundle-SymbolicName and Bundle-Version properties
* puts string "Bundle-SymbolicName:Bundle-Version" into separate file

Usage: python scan-manifests-for-bundle-info.py [options]

Options:
  -h, --help         Display help message and exit
  -o, --output       Path to result file
"""

import os
import zipfile
import sys
import re
import time
import getopt
                 
class Properties(object):
    
  def __init__(self):
    self._props = {}

  def getProperty(self, key):
    return self._props.get(key)
        
  def parse(self, lines):
    i = 0
    length = len(lines)
    while i < length:
      line = lines[i]
      if not line:
        i = i+1
        continue
      line = line.strip()
      while True:
        if i < length - 1:
          if lines[i+1].startswith(' '):
            line = line + lines[i+1].strip()
            i = i+1
          else:
            break
        else:
          break
      sepidx = line.find(':')
      key = line[:sepidx]
      value = line[sepidx+1:]
      self._props[key.strip()] = value.strip()
      i = i + 1

def printComponent(file, name, version):
  file.write(name)
  file.write(':')
  file.write(version)
  file.write('\n')
  return;

def readJar(jar):
  symbolicName = bundleVersion = ''
  manifest_bytes = jar.read('META-INF/MANIFEST.MF')
  p = Properties()
  p.parse(manifest_bytes.decode().split('\n'))
  symbolicName = p.getProperty('Bundle-SymbolicName')
  bundleVersion = p.getProperty('Bundle-Version')
  return symbolicName, bundleVersion;

def main(argv):
  output = 'third-party-bundles.properties';
  try:
    opts, args = getopt.getopt(argv, 'ho:', ['help', 'output='])
  except getopt.GetoptError:
    print (__doc__)
    sys.exit(2)
  if args:
    print (__doc__)
    sys.exit(-1)
  for opt in opts:
    if opt[0] in ('-h', '--help'):
      print (__doc__)
      sys.exit()
    if opt[0] in ('-o', '--output'):
      output = opt[1]
  
  output_file = open(output,'w')
  path_to_scan = './'
  files = os.listdir(path_to_scan)
  for file in files:
    if file.endswith('.jar'):
      print 'Reading manifest from jar: ' + file
      archive = zipfile.ZipFile(path_to_scan + '/' + file, 'r')
      name, version = readJar(archive)
      if name and version:
        printComponent(output_file, name, version)
      else:
        print '---WARNING: Jar file has no Bundle-SymbolicName and/or Bundle-Version in manifest'
  output_file.close()
  print('Finish successfully!')
  
if __name__=="__main__":
  main(sys.argv[1:])
