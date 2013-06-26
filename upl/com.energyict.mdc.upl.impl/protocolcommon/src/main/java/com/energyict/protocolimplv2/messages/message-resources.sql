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

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('Messages.notSupported', 'Message is not supported by the protocol', 'M', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('Messages.failed', '"Message "{0}" (id: {1}) failed to execute: {2}"', 'M', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('WriteWavecardParameters.writeExchangeStatusFailed', 'Could not write the exchange status parameter: {0}', 'M', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('WriteWavecardParameters.writeRadioAcknowledgeFailed', 'Could not write the radio acknowledge parameter: {0}', 'M', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('WriteWavecardParameters.writeRadioUserTimeoutFailed', 'Could not write the radio user timeout parameter: {0}', 'M', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.CHANGE_SMS_CENTER_NUMBER', 'Change the SMS center number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.smscenter.phonenumber', 'SMS center phone number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.CHANGE_DEVICE_PHONENUMBER', 'Change the device phone number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.device.phonenumber', 'Device phone number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.CHANGE_GPRS_IP_ADDRESS_AND_PORT', 'Change the GPRS IP address and port number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.ipaddress', 'IP address', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.portnumber', 'Port number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.CHANGE_WAKEUP_FREQUENCY', 'Change the GPRS wake up frequency', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('NetworkConnectivityMessage.wakeup.period', 'Wakeup period', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.ConfigureConverterMasterData', 'Configure the converter master data', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.convertertype', 'Converter type', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.converter.serialnumber', 'Converter serial number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.ConfigureGasMeterMasterData', 'Configure the gas meter master data', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.metertype', 'Meter type', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.metercaliber', 'Meter caliber', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.meter.serialnumber', 'Meter serial number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.ConfigureGasParameters', 'Configure the gas parameters', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.gas.density', 'Gas density', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.air.density', 'Air density', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.relative.density', 'Relative density', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.molecularnitrogen.percentage', 'Percentage molecular nitrogen', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.carbondioxide.percentage', 'Percentage carbondioxide', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.molecularhydrogen.percentage', 'Percantage molecular hydrogen', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.highercalorificvalue', 'Higher calorific value', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.EnableOrDisableDST', 'Enable or disable DST', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.enable.dst', 'Enable DST', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.WriteNewPDRNumber', 'Write the new PDR number', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ConfigurationChangeDeviceMessage.pdr', 'PDR number', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY', 'Activate or deactivate the temporary key', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.keyT.activationstatusT', 'Temporary encryption key status', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.timeduration', 'Time duration', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.CHANGE_EXECUTION_KEY', 'Change the execution key', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.executionkey', 'Execution key', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.CHANGE_TEMPORARY_KEY', 'Change the temporary key', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.temporarykey', 'Temporary key', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.BREAK_OR_RESTORE_SEALS', 'Break or restore seals', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.eventlogresetseal', 'Eventlog reset seal', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.restorefactorysettingsseal', 'Restore factory settings seal', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.restoredefaultsettingsseal', 'Restore default settings seal', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.statuschangeseal', 'Status change seal', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.remoteconversionparametersconfigseal', 'Remote conversion parameters config seal', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.remoteanalysisparametersconfigseal', 'Remote analysis parameters config seal', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.downloadprogramseal', 'Download program seal', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.restoredefaultpasswordseal', 'Restore default password seal', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.TEMPORARY_BREAK_SEALS', 'Temporary break seals', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.eventlogresetseal.breaktime', 'Eventlog reset seal break time', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.restorefactorysettingsseal.breaktime', 'Restore factory settings seal break time', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.restoredefaultsettingsseal.breaktime', 'Restore default settings seal break time', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.statuschangeseal.breaktime', 'Status change seal break time', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.remoteconversionparametersconfigseal.breaktime', 'Remote conversion parameters config seal break time', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.remoteanalysisparametersconfigseal.breaktime', 'Remote analysis parameters config seal break time', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.downloadprogramseal.breaktime', 'Download program seal break time', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('SecurityMessage.restoredefaultpasswordseal.breaktime', 'Restore default password seal break time', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF', 'Clear and disable the passive tariff', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATE', 'Send activity calendar via CodeTable and activation date', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME', 'Send activity calendar via CodeTable and activation date and time', 'L', 'Y', 'N', 'MDW', sysdate);

insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE', 'Firmware upgrade via user file with version number and activation date', 'L', 'Y', 'N', 'MDW', sysdate);
insert into nlsstrings (key, english, usage, fullclient, webclient, module, mod_date) values ('FirmwareDeviceMessage.upgrade.version', 'Firmware version number', 'L', 'Y', 'N', 'MDW', sysdate);