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

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('protocol.eventvalue', 'Protocol event value', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceActionMessage.SyncMasterdataForDC', 'Sync master data for DC', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceActionMessage.dcDeviceID', 'DC device ID', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceActionMessage.PauseDCScheduler', 'Pause DC scheduler', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceActionMessage.ResumeDCScheduler', 'Resume DC scheduler', 'L', 'Y', 'N', 'MDW', sysdate);