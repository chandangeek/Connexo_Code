/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommandsForRule', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.Command',
    sorters: [
        {
            property: 'displayName',
            direction: 'ASC'
        }
    ],
    storeId: 'CommandsForRule',
    requires: [
        'Mdc.model.Command'
    ]
});