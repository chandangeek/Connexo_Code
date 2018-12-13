/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommandLimitationRules',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommandLimitRule'
    ],
    model: 'Mdc.model.CommandLimitRule',
    storeId: 'CommandLimitationRules',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/crr/commandrules',
        reader: {
            type: 'json',
            root: 'commandrules'
        }
    }
});