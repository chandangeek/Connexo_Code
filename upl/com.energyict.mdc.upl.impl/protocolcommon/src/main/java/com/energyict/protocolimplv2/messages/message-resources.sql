-- This is a help and reminder document to inform the developer how and what message related translation
-- keys should be inserted in the nlsdatabase.

-- The database is located on labo.eict.local:1521/eiserver with username nls
-- The password is for security reasons NOT stored here

-- Each new DeviceMessageCategories that is added should have a corresponding translationkey,
-- something like 'DeviceMessageCategories.FIRMWARE' (keys are case sensitive)
-- also, add a key for the category description (DeviceMessageCategories.FIRMWARE.description)

-- Each new DeviceMessageSpec that is created or added to a category should have a corresponding translationkey,
-- something like 'ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND' (keys are case sensitive)

-- Each new DeviceMessageAttribute that is added to a DeviceMessageSpec should have a corresponding translationkey,
-- something like 'ActivityCalendarDeviceMessage.activitycalendar.codetable' (keys are case sensitive)

-- Use the following template to insert your keys:
-- insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('your key', 'english translation', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('noUserCredentialsAvailable', 'Cannot login, no user information is available. Make sure this application has access to the websocket of the online comserver first.', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('protocolTaskType.manualMeterReadings', 'Manual meter readings', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('AlarmConfigurationMessage.RESET_ALL_ERROR_BITS', 'Reset all error bits', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('userHasNoComServerMobileRights', 'User has no rights to user the ComServer Mobile', 'M', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('CSC-COM-151', 'Communication was interrupted: {0}', 'M', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.EnableG3PLCInterface', 'Enable G3 PLC interface', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER', 'Upgrade firmware with user file, activation date and image identifier', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('FirmwareDeviceMessage.image.identifier', 'Image identifier', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('MBusSetupDeviceMessage.mbusSerialNumber', 'MBus serial number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('MBusSetupDeviceMessage.Reset_MBus_Client', 'Reset MBus client', 'L', 'Y', 'N', 'MDW', sysdate);