/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.model.SecurityPreviewProperties', {
    extend: 'Uni.model.Version',

    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'keyType', type: 'auto'},
        {name: 'storageMethod', type: 'auto'},
        {name: 'trustStoreId', type: 'int'},
        {name: 'duration', type: 'auto', useNull: true},
        {name: 'viewLevels', type: 'auto', useNull: true},
        {name: 'editLevels', type: 'auto', useNull: true},
        {name: 'defaultViewLevels', type: 'auto', useNull: true},
        {name: 'defaultEditLevels', type: 'auto', useNull: true},
        {name: 'currentProperties', type: 'auto'},
        {name: 'tempProperties', type: 'auto'},
        {
            name: 'viewLevelsInfo',
            persist: false,
            mapping: function (data) {
                return {
                    levels: data.viewLevels,
                    defaultLevels: data.defaultViewLevels
                };
            }
        },
        {
            name: 'editLevelsInfo',
            persist: false,
            mapping: function (data) {
                return {
                    levels: data.editLevels,
                    defaultLevels: data.defaultEditLevels
                };
            }
        },
        {
            name: 'isKey',
            persist: false,
            mapping: function (data) {
                return Ext.isEmpty(data) || Ext.isEmpty(data.keyType) ? false : data.keyType.isKey;
            }
        },
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
        url: '/api/dtc/securityaccessors/previewproperties',
        reader: {
            type: 'json'
        }
    }

});