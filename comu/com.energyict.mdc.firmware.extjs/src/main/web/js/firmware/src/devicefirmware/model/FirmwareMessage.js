/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.model.FirmwareMessage', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'uploadOption', type: 'string', useNull: true},
        {name: 'localizedValue', type: 'string', useNull: true},
        {name: 'releaseDate', type: 'int', useNull: true}
    ],

    proxy: {
        type: 'rest',
        url: '/api/fwc/devices/{deviceId}/firmwaremessages',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'firmwareCommand'
        }
    },
    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function () {
                return 'Uni.property.model.Property';
            }
        }
    ]
});