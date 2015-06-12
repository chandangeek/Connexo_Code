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

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT_AND_ACTIVATION_DATE', 'Open contactor with output ID and activation date', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT_AND_ACTIVATION_DATE', 'Close contactor with output ID and activation date', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.groupName', 'Group name', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.bitSync', 'Bit sync', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.zeroCrossAdjust', 'Zero cross adjust', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.txGain', 'TX gain', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.rxGain', 'RX gain', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.addCredit', 'Add credit', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.minCredit', 'Min credit', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.IDISRunRepeaterCallNow', 'IDIS Run repeater call now', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.IDISRunNewMeterDiscoveryCallNow', 'IDIS Run new meter discovery now', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.IDISRunAlarmDiscoveryCallNow', 'IDIS Run alarm discovery now', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.IDISWhitelistConfiguration', 'IDIS Local whitelist configuration', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.IDISOperatingWindowConfiguration', ' IDIS Operating window configuration', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.IDISPhyConfiguration', 'IDIS phy configuration', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('PLCConfigurationDeviceMessage.IDISCreditManagementConfiguration', 'IDIS Credit management configuration', 'L', 'Y', 'N', 'MDW', sysdate);