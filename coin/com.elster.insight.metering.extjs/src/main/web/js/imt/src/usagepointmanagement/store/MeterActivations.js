/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.MeterActivations', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeterActivations',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/meteractivations',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'meterActivations'
        }
    }
});