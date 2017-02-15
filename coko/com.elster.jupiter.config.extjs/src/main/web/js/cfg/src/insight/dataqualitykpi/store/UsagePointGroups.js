/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.store.UsagePointGroups', {
    extend: 'Ext.data.Store',
    fields: ['id', 'displayValue'],
    proxy: {
        type: 'rest',
        url: '/api/val/field/usagepointgroups',
        reader: {
            type: 'json',
            root: 'usagePointGroups'
        }
    }
});