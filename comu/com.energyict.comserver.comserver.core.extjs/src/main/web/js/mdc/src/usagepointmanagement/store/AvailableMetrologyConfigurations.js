/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.store.AvailableMetrologyConfigurations', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.MetrologyConfiguration',
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/{usagePointId}/availablemetrologyconfigurations',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'metrologyConfigurations'
        }
    }
});