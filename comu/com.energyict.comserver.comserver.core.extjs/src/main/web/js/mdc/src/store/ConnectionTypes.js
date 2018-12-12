/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ConnectionTypes', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    requires: [
        'Mdc.model.ConnectionType'
    ],
    model: 'Mdc.model.ConnectionType',
    storeId: 'ConnectionTypes',
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    proxy: {
        type: 'rest',
        url: '../../api/plr/devicecommunicationprotocols/{protocolId}/connectiontypes',
        reader: {
            type: 'json'
        }
    }

});
