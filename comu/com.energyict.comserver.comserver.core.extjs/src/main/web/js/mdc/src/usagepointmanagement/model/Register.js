/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.model.Register', {
    extend: 'Ext.data.Model',
    fields: ['dataUntil', 'measurementTime', 'readingType', 'deviceRegisters'],
    proxy: {
        type: 'rest',
        url: '/api/upr/usagepoints/{usagePointId}/registers',
        reader: {
            type: 'json'
        }
    }
});