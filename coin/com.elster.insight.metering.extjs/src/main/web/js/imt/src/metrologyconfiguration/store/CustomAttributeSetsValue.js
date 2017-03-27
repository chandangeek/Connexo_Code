/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.store.CustomAttributeSetsValue', {
    extend: 'Ext.data.Store',
    // model: 'Imt.metrologyconfiguration.model.MetrologyConfigurationWithCAS',
    // autoLoad: true,
    fields: [
        'id',
        'name'
    ],
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations/{id}/usagepoint/{upId}',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'customPropertySets'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});