/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceConfigurationResults',{
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ValidationResults',
    storeId: 'DeviceTypes',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/validationrulesets/validationmonitoring/configurationview',
        reader: {
            type: 'json'
        }
    }
});
