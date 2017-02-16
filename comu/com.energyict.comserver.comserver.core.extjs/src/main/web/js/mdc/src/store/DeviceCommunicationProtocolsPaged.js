/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceCommunicationProtocolsPaged', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceCommunicationProtocol'
    ],
    model: 'Mdc.model.DeviceCommunicationProtocol',
    storeId: 'DeviceCommunicationProtocolsPaged',
    proxy: {
        type: 'rest',
        url: '../../api/plr/devicecommunicationprotocols',
        reader: {
            type: 'json',
            root: 'DeviceProtocolPluggableClass'
        }
    }

});