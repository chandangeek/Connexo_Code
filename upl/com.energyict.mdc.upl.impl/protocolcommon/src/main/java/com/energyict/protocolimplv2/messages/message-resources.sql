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


insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST', 'Add phonenumbers to white list', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.whitelist.phonenumbers', 'Phonenumbers', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS', 'Change the GPRS user credentials', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS', 'Change the GPRS apn credentials', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DisplayDeviceMessage.consumer.CONSUMER_MESSAGE_CODE_TO_PORT_P1', 'Send a code message to the P1 port', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DisplayDeviceMessage.consumer.CONSUMER_MESSAGE_TEXT_TO_PORT_P1', 'Send a text message to the P1 port', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DisplayDeviceMessage.consumer.p1', 'P1 message', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceActionMessage.GLOBAL_METER_RESET', 'Global meter reset', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS', 'Configure the load limit parameters', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS', 'Set the load limit emergency profiles', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION', 'Clear the load limit configuration', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.parameters.normalthreshold', 'Normal threshold', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.parameters.emergencythreshold', 'Emergency threshold', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.parameters.overthresholdduration', 'Threshold exceed duration', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.parameters.emergencyprofileid', 'Emergency profile ID', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.parameters.emergencyProfileActivationDate', 'Emergency profile activation date', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.parameters.emergencyProfileDuration', 'Emergency profile duration', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadBalanceDeviceMessage.parameters.emergencyprofileidlookup', 'Emergency profile group IDs', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceMessageCategories.ADVANCED_TEST', 'Advanced test messages', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('AdvancedTestMessage.xmlconfig', 'XML', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('AdvancedTestMessage.XML_CONFIG', 'XML configuration', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceMessageCategories.LOAD_PROFILES', 'LoadProfile messages', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST', 'Partial load profile request', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST', 'Load profile register request', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('DeviceMessageCategories.CONFIGURATION_CHANGE', 'Configuration changes', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.WriteWavecardParameters', 'Write Wavecard parameter', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('WriteWavecardParameters.writeExchangeStatus', 'Write exchange status', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('WriteWavecardParameters.writeRadioAcknowledge', 'Write radio acknowledge', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('WriteWavecardParameters.writeRadioUserTimeout', 'Write radio user timeout', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('Messages.notSupported', 'Message is not supported by the protocol', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('WriteWavecardParameters.writeExchangeStatusFailed', 'Could not write the exchange status parameter: {0}', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('WriteWavecardParameters.writeRadioAcknowledgeFailed', 'Could not write the radio acknowledge parameter: {0}', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('WriteWavecardParameters.writeRadioUserTimeoutFailed', 'Could not write the radio user timeout parameter: {0}', 'L', 'Y', 'N', 'MDW', sysdate);
