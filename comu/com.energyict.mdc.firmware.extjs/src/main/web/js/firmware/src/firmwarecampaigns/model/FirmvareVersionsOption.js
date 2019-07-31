/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.model.FirmvareVersionsOption', {
    extend: 'Ext.data.Model',
    fields: [
        {
              name: 'masterFirmwareCheck',
              persist: false,
              mapping:  function (data) {
                   var masterData = data && data.checkOptions && data.checkOptions['MASTER_FIRMWARE_CHECK'] && data.checkOptions['MASTER_FIRMWARE_CHECK'].activated;
                   return masterData && data.checkOptions['MASTER_FIRMWARE_CHECK'].statuses ? data.checkOptions['MASTER_FIRMWARE_CHECK'].statuses : [];
              },
        },
        {
              name: 'targetFirmwareCheck',
              persist: false,
              mapping:  function (data) {
                   var targetData = data && data.checkOptions && data.checkOptions['TARGET_FIRMWARE_STATUS_CHECK'] && data.checkOptions['TARGET_FIRMWARE_STATUS_CHECK'].activated;
                   return targetData && data.checkOptions['TARGET_FIRMWARE_STATUS_CHECK'].statuses ? data.checkOptions['TARGET_FIRMWARE_STATUS_CHECK'].statuses : [];
              },
        },
        {
              name: 'curFirmwareCheck',
              persist: false,
              mapping:  function (data) {
                   var currFirmData = data && data.checkOptions && data.checkOptions['CURRENT_FIRMWARE_CHECK'] && data.checkOptions['CURRENT_FIRMWARE_CHECK'].activated;
                   return currFirmData ? ['COMMON'] : []
              }
        }
    ]
});