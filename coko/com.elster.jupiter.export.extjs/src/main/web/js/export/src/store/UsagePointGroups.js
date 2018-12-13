/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.UsagePointGroups', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.UsagePointGroup',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/export/fields/usagepointgroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'usagePointGroups'
        }
    }
});