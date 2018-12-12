/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.CommunicationTasks', {
    extend: 'Uni.data.store.Filterable',
    requires: [
        'Dsh.model.CommunicationTask',
        'Dsh.util.FilterStoreHydrator'
    ],
    model: 'Dsh.model.CommunicationTask',
    hydrator: 'Dsh.util.FilterStoreHydrator',
    autoLoad: false,
    remoteFilter: true,

    proxy: {
        type: 'ajax',
        url: '/api/dsr/communications',
        reader: {
            type: 'json',
            root: 'communicationTasks',
            totalProperty: 'total'
        }
    }
});


