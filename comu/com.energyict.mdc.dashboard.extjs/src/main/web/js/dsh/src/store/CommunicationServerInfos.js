/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.store.CommunicationServerInfos', {
    extend: 'Ext.data.Store',
    storeId: 'CommunicationServerInfos',
    requires: ['Dsh.model.CommunicationServerInfo'],
    model: 'Dsh.model.CommunicationServerInfo',
    autoLoad: false,

    groupers: [
        {
            direction: 'ASC',
            property: 'status'
        }
    ]
});