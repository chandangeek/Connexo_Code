/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableSlaveDeviceTypes',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceType'
    ],
    model: 'Mdc.model.DeviceType',
    storeId: 'AvailableSlaveDeviceTypes',
    filters: [
        function(record) {
            return (record.get('deviceTypePurpose') === 'DATALOGGER_SLAVE' || record.get('deviceTypePurpose') === 'MULTI_ELEMENT_SLAVE') && record.get('activeDeviceConfigurationCount') > 0;
        }
    ],
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'deviceTypes'
        }
    }
});
