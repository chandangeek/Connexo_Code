/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.QueueType', {
    extend: 'Ext.data.Model',
    fields: [
        'name'
        //'value'
    ],
    idProperty: 'name',
    proxy: {
        type: 'rest',
        url: '/api/msg/destinationspec/queuetypenames',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
