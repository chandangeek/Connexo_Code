/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceCommunicationProtocol', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'deviceProtocolVersion'
    ],
    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    idProperty: 'id',
    proxy: {
        type: 'rest',
        url: '../../api/plr/devicecommunicationprotocols',
        reader: {
            type: 'json'
        }
    }
});