/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.model.Webservice', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'direction', type: 'auto'},
        {name: 'type', type: 'string'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/ws/webservices',
        reader: {
            type: 'json'
        }
    }
});