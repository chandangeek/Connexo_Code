/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.MessageQueues', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.MessageQueue',
    autoLoad: false,


    proxy: {
        type: 'rest',
        url: '/api/msg/destinationspec',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'destinationSpecs'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});