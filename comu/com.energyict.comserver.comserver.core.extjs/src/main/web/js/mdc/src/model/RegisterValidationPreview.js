/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.RegisterValidationPreview', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'validationActive',
            type: 'boolean'
        },
        {
            name: 'dataValidated',
            type: 'boolean'
        },
        {
            name: 'lastChecked',
            type: 'auto'
        }
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        url: '/api/ddr/devices/{deviceId}/registers/{registerID}/validationpreview',
        reader: {
            type: 'json'
        }
    }
});