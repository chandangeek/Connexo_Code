/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableDataLoggerSlaveDeviceTypes',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceType'
    ],
    model: 'Mdc.model.DeviceType',
    storeId: 'AvailableDataLoggerSlaveDeviceTypes',
    filters: [
        function(record) {
            return record.get('deviceTypePurpose') === 'DATALOGGER_SLAVE';
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
