/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.store.Issues', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.Issue',
    pageSize: 10,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/itk/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
