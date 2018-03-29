/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.CustomTaskTypes', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.CustomTaskType',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/ctk/customtask/types',
        reader: {
            type: 'json',
            root: ''
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});