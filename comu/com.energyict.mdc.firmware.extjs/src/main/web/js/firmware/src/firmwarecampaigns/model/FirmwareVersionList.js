/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.model.FirmwareVersionList', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'firmwareVersion', type: 'string', useNull: true},
        {name: 'imageIdentifier', type: 'string', useNull: true},
        {name: 'firmwareFile', useNull: true},
        {name: 'fileSize', type: 'number', useNull: true},
        {
            name: 'type',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.firmwareType ? data.firmwareType : '';
            }
        },
        {
            name: 'status',
            type: 'string',
            persist: false,
            mapping: function (data) {
                return data.firmwareStatus ? data.firmwareStatus : '';
            }
        },
        {
            name: 'rank',
            type: 'number',
            persist: false,
        },
        {
            name: 'meterFirmwareDependency',
            useNull: true,
            persist: false
        },
        {
            name: 'communicationFirmwareDependency',
            useNull: true,
            persist: false
        },
        {
            name: 'firmwareType',
            type: 'string'
        },
        {
            name: 'firmwareStatus',
            type: 'string'
        },
    ]
});