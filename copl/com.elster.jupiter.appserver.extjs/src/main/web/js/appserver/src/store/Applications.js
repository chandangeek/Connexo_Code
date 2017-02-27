/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.Applications', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.Application',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/tsk/task/applications',
        reader: {
            type: 'json',
            root: 'applications'
        }
    },

    sorters: [
        {
            property: 'name',
            direction: 'ASC'
        }
    ]

});