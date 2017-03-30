/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterGroup'
    ],
    model: 'Mdc.model.RegisterGroup',
    storeId: 'RegisterGroups',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/registergroups',
        reader: {
            type: 'json',
            root: 'registerGroups'
        }
    }
});
