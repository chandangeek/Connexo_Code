/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.QueuesType', {
    extend: 'Ext.data.Store',
    requires: [
        'Apr.model.QueueType'
    ],
    model: 'Apr.model.QueueType',
    proxy: {
        type: 'rest',
        url: '/api/msg/destinationspec/queuetypenames',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
    }
});
