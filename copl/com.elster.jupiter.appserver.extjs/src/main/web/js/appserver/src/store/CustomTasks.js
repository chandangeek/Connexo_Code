/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.CustomTasks', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.CustomTask',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/ctk/customtask',
        reader: {
            type: 'json',
            root: ''
        }
    }
});