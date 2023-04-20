/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.model.FirmwareVersionsOption', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'masterFirmwareCheck',
            persist: false,
            mapping: function (data) {
                var masterData = data && data['MASTER_FIRMWARE_CHECK'] && data['MASTER_FIRMWARE_CHECK'].activated;
                return masterData && data['MASTER_FIRMWARE_CHECK'].statuses ? data['MASTER_FIRMWARE_CHECK'].statuses : [];
            },
        },
        {
            name: 'targetFirmwareCheck',
            persist: false,
            mapping: function (data) {
                var targetData = data && data['TARGET_FIRMWARE_STATUS_CHECK'] && data['TARGET_FIRMWARE_STATUS_CHECK'].activated;
                return targetData && data['TARGET_FIRMWARE_STATUS_CHECK'].statuses ? data['TARGET_FIRMWARE_STATUS_CHECK'].statuses : [];
            },
        },
        {
            name: 'curFirmwareCheck',
            persist: false,
            mapping: function (data) {
                var currFirmData = data && data['CURRENT_FIRMWARE_CHECK'] && data['CURRENT_FIRMWARE_CHECK'].activated;
                return currFirmData ? ['COMMON'] : []
            }
        }
    ]
});
