/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceValidationResults',{
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ValidationResultsDataView',
    storeId: 'DeviceTypes',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/validationrulesets/validationmonitoring/dataview',
        reader: {
            type: 'json'
        }
    }
});
