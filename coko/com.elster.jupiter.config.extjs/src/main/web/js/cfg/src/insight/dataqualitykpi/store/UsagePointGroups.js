/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.store.UsagePointGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.insight.dataqualitykpi.model.UsagePointGroup'
    ],
    model: 'Cfg.insight.dataqualitykpi.model.UsagePointGroup',
    proxy: {
        type: 'rest',
        url: '/api/dqk/fields/usagePointGroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'usagePointGroups'
        }
    }
});