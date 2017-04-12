/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.store.MetrologyConfigurationsHistory', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointhistory.model.MetrologyConfigurationVersion',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/history/metrologyConfigurations',
        reader: {
            type: 'json',
            root: 'data'
        },
        startParam: undefined,
        limitParam: undefined,
        pageParam: undefined
    }
});
