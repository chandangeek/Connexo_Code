/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.Communications', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.connection.CommunicationTask'
    ],
    model: 'Dsh.model.connection.CommunicationTask',
    autoLoad: false,
    remoteFilter: true,
    url: '/api/dsr/connections/',
    communicationsPostfix: '/latestcommunications',
    proxy: {
        type: 'ajax',
        reader: {
            type: 'json',
            root: 'communications',
            totalProperty: 'total'
        }
    },
    setConnectionId: function (id) {
        this.getProxy().url = this.url + id + this.communicationsPostfix
    }
});



