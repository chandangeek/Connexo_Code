/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceSecuritySetting', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'securitySuite', type: 'auto', useNull: true},
        {name: 'authenticationLevel', type: 'auto', useNull: true},
        {name: 'encryptionLevel', type: 'auto', useNull: true},
        {name: 'requestSecurityLevel', type: 'auto', useNull: true},
        {name: 'responseSecurityLevel', type: 'auto', useNull: true}
    ],

    associations: [
        {name: 'client', type: 'hasOne', model: 'Uni.property.model.Property', associationKey: 'client', getterName: 'getClient', setterName: 'setClient', foreignKey: 'client'},
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/securityproperties'
    }
});