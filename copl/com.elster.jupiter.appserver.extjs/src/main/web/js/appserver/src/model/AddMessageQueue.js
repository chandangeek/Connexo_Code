/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.AddMessageQueue', {
    extend: 'Ext.data.Model',

    fields: [
        'name',
        'queueTypeName'
    ],


    proxy: {
        type: 'rest',
        url: '/api/msg/destinationspec',
        reader: {
            type: 'json'
        }
    }
});