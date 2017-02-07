/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceDiscoveryProtocol', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'javaClassName',
        'deviceProtocolVersion'
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mdc/devicediscoveryprotocols',
        reader: {
            type: 'json'
        }
    }
})
;