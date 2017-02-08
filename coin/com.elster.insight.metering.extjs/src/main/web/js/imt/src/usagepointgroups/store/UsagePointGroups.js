/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.store.UsagePointGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.usagepointgroups.model.UsagePointGroup'
    ],
    model: 'Imt.usagepointgroups.model.UsagePointGroup',
    storeId: 'UsagePointGroups',    
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepointgroups',
        reader: {
            type: 'json',
            root: 'usagePointGroups'
        }
    }
});
