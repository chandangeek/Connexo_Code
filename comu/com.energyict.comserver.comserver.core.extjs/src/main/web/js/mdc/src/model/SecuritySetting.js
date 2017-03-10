/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.SecuritySetting', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.AuthenticationLevel',
        'Mdc.model.EncryptionLevel',
        'Mdc.model.ExecutionLevel'
    ],
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'authenticationLevelId', type: 'number', useNull: true},
        {name: 'authenticationLevel', persist: false},
        {name: 'encryptionLevelId', type: 'number', useNull: true },
        {name: 'encryptionLevel', persist: false},
        {name: 'executionLevels', persist: false}
    ],
    associations: [
        {name: 'authenticationLevel', type: 'hasOne', model: 'Mdc.model.AuthenticationLevel'},
        {name: 'encryptionLevel', type: 'hasOne', model: 'Mdc.model.EncryptionLevel'},
        {name: 'executionLevels', type: 'hasMany', model: 'Mdc.model.ExecutionLevel', associationKey: 'executionLevels',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.ExecutionLevel';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/securityproperties'
    }
});