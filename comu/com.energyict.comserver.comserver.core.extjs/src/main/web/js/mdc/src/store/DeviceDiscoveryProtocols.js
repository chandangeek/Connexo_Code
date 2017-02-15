/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceDiscoveryProtocols', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceDiscoveryProtocol'
    ],
    model: 'Mdc.model.DeviceDiscoveryProtocol',
    storeId: 'DeviceDiscoveryProtocols',
    autoLoad: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    proxy: {
        type: 'rest',
        url: '/api/plr/devicediscoveryprotocols',
        reader: {
            type: 'json',
            root: 'deviceDiscoveryProtocolInfos'
        }
    }

});