-- This is a help and reminder document to inform the developer how and what message related translation
-- keys should be inserted in the nlsdatabase.

-- The database is located on labo.eict.local:1521/eiserver with username nls
-- The password is for security reasons NOT stored here

-- Each new DeviceMessageCategories that is added should have a corresponding translationkey,
-- something like 'DeviceMessageCategories.FIRMWARE' (keys are case sensitive)

-- Each new DeviceMessageSpec that is created or added to a category should have a corresponding translationkey,
-- something like 'ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND' (keys are case sensitive)

-- Each new DeviceMessageAttribute that is added to a DeviceMessageSpec should have a corresponding translationkey,
-- something like 'ActivityCalendarDeviceMessage.activitycalendar.codetable' (keys are case sensitive)

-- Use the following template to insert your keys:
-- insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('your key', 'english translation', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE', 'Firware upgrade via user file', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('FirmwareDeviceMessage.UPGRADE_FIRMWARE_ACTIVATE', 'Active last uploaded firmware', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE', 'Firware upgrade via user file with activation date', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('FirmwareDeviceMessage.UPGRADE_FIRMWARE_URL', 'Firware upgrade via url', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('FirmwareDeviceMessage.UPGRADE_FIRMWARE_URL_AND_ACTIVATE', 'Firware upgrade via url with activation date', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceMessageCategories.FIRMWARE', 'Device actions', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceActionMessage.BILLING_RESET', 'Billing reset', 'L', 'Y', 'N', 'MDW', sysdate);
