/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.store.MetrologyConfiguration', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.MetrologyConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations',
        reader: {
            type: 'json',
            root: 'metrologyconfigurations'
        },
        timeout: 640000,
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});