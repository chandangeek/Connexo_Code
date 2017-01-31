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
        {name: 'authenticationLevel', type: 'auto', useNull: true},
        {name: 'encryptionLevel', type: 'auto', useNull: true},
        {name: 'status', type: 'auto', useNull: true},
        {name: 'userHasEditPrivilege', type: 'boolean', useNull: true},
        {name: 'userHasViewPrivilege', type: 'boolean', useNull: true},
        {name: 'saveAsIncomplete', type: 'boolean', userNull: true}
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
        url: '/api/ddr/devices/{deviceId}/securityproperties'
    }
});