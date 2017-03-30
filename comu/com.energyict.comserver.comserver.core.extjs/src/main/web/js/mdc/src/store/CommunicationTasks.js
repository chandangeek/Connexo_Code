/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.CommunicationTasks',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationTask'
    ],
    model: 'Mdc.model.CommunicationTask',
    storeId: 'CommunicationTasks',
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '/api/cts/comtasks',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});