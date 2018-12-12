/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.store.PurposesWithValidationRuleSets', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.MetrologyContract',
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations/{metrologyConfigurationId}/contracts',
        reader: {
            type: 'json',
            root: 'contracts'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
