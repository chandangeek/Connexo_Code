/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceTransition', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'device', defaultValue: null},
        {name: 'transitionNow', type: 'boolean'},
        {name: 'effectiveTimestamp', type: 'long', useNull: true}
    ],

    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/transitions'
    }
});
