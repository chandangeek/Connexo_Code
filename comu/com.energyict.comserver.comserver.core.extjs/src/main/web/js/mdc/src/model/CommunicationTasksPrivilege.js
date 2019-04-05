/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.CommunicationTasksPrivilege', {
    extend: 'Ext.data.Model',
    idProperty: 'privilege',
    fields: [
        {
            name: 'privilege',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        }
        // need roles also
    ],

    proxy: {
        type: 'rest',
        url: '/api/cts/comtasks/privileges',
        reader: {
            type: 'json',
            root: 'data',
            idProperty: 'privilege'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
