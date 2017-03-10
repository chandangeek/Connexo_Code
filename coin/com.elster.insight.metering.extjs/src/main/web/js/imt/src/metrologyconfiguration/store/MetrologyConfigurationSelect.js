/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.store.MetrologyConfigurationSelect', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.MetrologyConfiguration',
    //autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'metrologyconfigurations'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    },
    listeners: {
        load: function(store, records) {            
            store.insert(0, [{id:0, name:'NONE'}]);
        }
    }
});