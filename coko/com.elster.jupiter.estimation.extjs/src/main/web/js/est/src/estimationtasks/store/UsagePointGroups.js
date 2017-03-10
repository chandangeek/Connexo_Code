/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.store.UsagePointGroups', {
    extend: 'Ext.data.Store',
    requires: ['Est.estimationtasks.model.UsagePointGroup'],
    model: 'Est.estimationtasks.model.UsagePointGroup',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/est/field/usagepointgroups',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'usagePointGroups'
        }
    }
});
