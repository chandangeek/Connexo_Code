/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CodeTables', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CodeTable'
    ],
    model: 'Mdc.model.CodeTable',
    storeId: 'CodeTables',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '../../api/plr/calendars',
        reader: {
            type: 'json',
            root: 'Code'
        }
    }
});