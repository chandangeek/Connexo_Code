/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.datavalidation.store.Issues', {
    extend: 'Ext.data.Store',
    model: 'Imt.datavalidation.model.Issue',
    pageSize: 10,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/isu/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
