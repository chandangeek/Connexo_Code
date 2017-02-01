/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.MetrologyConfigurations', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.MetrologyContract'
    ],
    model: 'Cfg.model.MetrologyContract',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/val/field/metrologyconfigurations',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metrologyConfigurations'
        }
    }
});
