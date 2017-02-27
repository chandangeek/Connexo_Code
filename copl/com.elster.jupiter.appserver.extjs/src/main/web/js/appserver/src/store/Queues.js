/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.Queues', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.Queue',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/tsk/task/queues',
        reader: {
            type: 'json',
            root: 'queues'
        }
    },

    sorters: [
        {
            property: 'queue',
            direction: 'ASC'
        }
    ]
});