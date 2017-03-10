/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.LinkableMetrologyConfiguration',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/metrologyconfiguration/linkable',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'metrologyConfigurations'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});