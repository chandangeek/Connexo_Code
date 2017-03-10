/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.LogbookTypesOfDeviceType', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Mdc.model.LogbookTypeOfDeviceType'
    ],
    model: 'Mdc.model.LogbookTypeOfDeviceType',
    storeId: 'LogbookTypesOfDeviceType',
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/logbooktypes',
        reader: {
            type: 'json',
            root: 'logbookTypes'
        }
    }
});
