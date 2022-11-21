/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.Issue',
    pageSize: 10,
    autoLoad: false,
    proxy: {
        type: 'rest',
        timeout: 120000,
        url: '/api/isu/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
