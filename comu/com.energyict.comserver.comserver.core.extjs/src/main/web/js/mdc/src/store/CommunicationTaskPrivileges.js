/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommunicationTaskPrivileges', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationTasksPrivilege'
    ],
    model: 'Mdc.model.CommunicationTasksPrivilege',
    storeId: 'CommunicationTaskPrivileges',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/cts/comtasks/privileges',
        reader: {
            type: 'json',
            root: 'data',
            idProperty: 'privilege'
        },
        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});