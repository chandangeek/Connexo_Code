/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.SecuritySetting', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.SecuritySuite',
        'Mdc.model.AuthenticationLevel',
        'Mdc.model.EncryptionLevel',
        'Mdc.model.RequestSecurityLevel',
        'Mdc.model.ResponseSecurityLevel',
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'securitySuite', persist: false},
        {name: 'securitySuiteId', type: 'number', useNull: true },
        {name: 'authenticationLevelId', type: 'number', useNull: true},
        {name: 'authenticationLevel', persist: false},
        {name: 'encryptionLevelId', type: 'number', useNull: true },
        {name: 'encryptionLevel', persist: false},
        {name: 'requestSecurityLevelId', type: 'number', useNull: true },
        {name: 'requestSecurityLevel', persist: false},
        {name: 'responseSecurityLevelId', type: 'number', useNull: true },
        {name: 'responseSecurityLevel', persist: false}
    ],
    associations: [
        {name: 'securitySuite', type: 'hasOne', model: 'Mdc.model.SecuritySuite'},
        {name: 'client', type: 'hasOne', model: 'Uni.property.model.Property', associationKey: 'client', getterName: 'getClient', setterName: 'setClient', foreignKey: 'client'},
        {name: 'authenticationLevel', type: 'hasOne', model: 'Mdc.model.AuthenticationLevel'},
        {name: 'encryptionLevel', type: 'hasOne', model: 'Mdc.model.EncryptionLevel'},
        {name: 'requestSecurityLevel', type: 'hasOne', model: 'Mdc.model.RequestSecurityLevel'},
        {name: 'responseSecurityLevel', type: 'hasOne', model: 'Mdc.model.ResponseSecurityLevel'},
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/securityproperties'
    }
});