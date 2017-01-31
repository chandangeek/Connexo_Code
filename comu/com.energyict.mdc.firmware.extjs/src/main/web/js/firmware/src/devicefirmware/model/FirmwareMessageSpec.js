/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.model.FirmwareMessageSpec', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'string', useNull: true},
        {name: 'localizedValue', type: 'string'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/fwc/devices/{deviceId}/firmwaremessagespecs',
        reader: 'json'
    },
    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ]
});