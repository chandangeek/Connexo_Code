# Steps to install and configure Coverity
## Read instructions found on the Coverity site (Help->Coverity Help Center)
* Click on "Installation and Deployment Guide"
* Click on "Coverity Analysis User and Administrator Guide"

## Download the software
* Click on "Help->Downloads..."
* Select the "cov-analysis-linux-64-xxx.sh" package
* Click on "Download" and transfer to Puppet VM

## Update Puppet
Edit the coverity install script with the new version on the se_puppet VM:

`/etc/puppetlabs/code/modules/linux_vm/manifests/coverity.pp`

Change the script name to the new script
Change cov_location to the name of the new directory

## Create the capture configuration
### Steps
* Log onto a VM
* Check out the tree
* Set up the environment
* Remove the previous configuration
* Create the new configuration

### Example
```
ssh mheinze@HIC006388
cd /home2/src/ntl
cd coverity
KEY_FILE=coverity/ak-coverity.swtools.honeywell.com-8443
COV_DIR='--dir build/idir'
BIN_DIR="$COVERITY_TOOL_HOME/bin"
git rm template*/* coverity_config*
cd ..
$BIN_DIR/cov-configure --config coverity/coverity_config.xml --python
$BIN_DIR/cov-configure --config coverity/coverity_config.xml --java
$BIN_DIR/cov-configure --config coverity/coverity_config.xml --swift
$BIN_DIR/cov-configure --config coverity/coverity_config.xml --javascript
rm coverity/configure-log.txt coverity/coverity_config.xml.bak
git add coverity/coverity_config*
git add coverity/template*
git commit
```
