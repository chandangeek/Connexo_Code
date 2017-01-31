/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ldr.model.Privilege', {
    extend: 'Ext.data.Model',

    fields: [
        'name', 'applicationName'
    ],

    proxy: {
        type: 'rest',
        url: '/api/usr/users/privileges',

        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,

        reader: {
            type: 'json',
            root: 'privileges'
        }
    }
});

