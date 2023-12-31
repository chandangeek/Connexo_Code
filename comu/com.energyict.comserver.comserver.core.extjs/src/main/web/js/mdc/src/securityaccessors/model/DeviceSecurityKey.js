/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.model.DeviceSecurityKey', {
    extend: 'Uni.model.Version',

    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'expirationTime', type: 'int'},
        {name: 'modificationDate', type: 'int'},
        {name: 'serviceKey', type: 'boolean'},
        {name: 'status', type: 'string'},
        {name: 'canGeneratePassiveKey', type: 'boolean', defaultValue:false, persist:false},
        {name: 'swapped', type: 'boolean', defaultValue:false},
        {name: 'hasTempValue', type: 'boolean', defaultValue:false},
        {name: 'defaultServiceKey', type: 'string'},
        {name: 'keyType', type: 'string'},
        'viewLevels',
        'editLevels'
    ],
    associations: [
        {
            name: 'currentProperties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'currentProperties', foreignKey: 'currentProperties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        },
        {
            name: 'tempProperties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'tempProperties', foreignKey: 'tempProperties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{deviceId}/securityaccessors/keys',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceId) {
            this.url = this.urlTpl.replace('{deviceId}', deviceId);
        }
    }

});