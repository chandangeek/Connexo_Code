/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.QueueType', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'value'
    ],
    proxy: {
        type: 'rest',
        url: '/api/msg/destinationspec/queuetypenames',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
