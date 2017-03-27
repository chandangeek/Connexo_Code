/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.store.CustomAttributeSetsValue', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.MetrologyConfigurationWithCAS',
    // fields: [
    //     'id'
    // ],
    // autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations/{id}/usagepoint/{upId}',
        timeout: 240000,
        reader: {
            type: 'json',
            // root: 'Info[0]'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});