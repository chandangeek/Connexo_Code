/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.model.DeviceSecurityCertificate', {
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
        {name: 'lastReadDate', type: 'int', useNull:true},
        {name: 'status', type: 'string'},
        {name: 'swapped', type: 'boolean', defaultValue:false},
        {name: 'hasTempValue', type: 'boolean', defaultValue:false},
        {name: 'editable', type: 'boolean'}
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
        tempUrl: '/api/ddr/devices/{deviceId}/securityaccessors/certificates',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceId) {
            this.url = this.tempUrl.replace('{deviceId}', deviceId);
        }
    }

});