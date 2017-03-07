/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.model.SecurityAccessor', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.securityaccessors.model.KeyType'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'keyType', type: 'auto'},
        {name: 'validityPeriod', type: 'auto', useNull: true},
        {name: 'viewLevels', type: 'auto', useNull: true},
        {name: 'editLevels', type: 'auto', useNull: true},
        {name: 'defaultViewLevels', type: 'auto', useNull: true},
        {name: 'defaultEditLevels', type: 'auto', useNull: true},
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
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/securityaccessors',
        reader: {
            type: 'json',
            root: 'securityaccessors'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    },
    associations: [
        {
            name: 'keyType',
            type: 'hasOne',
            model: 'Mdc.securityaccessors.model.KeyType',
            associationKey: 'keyType',
            getterName: 'getKeyType',
            setterName: 'setKeyType',
            foreignKey: 'keyType'
        }
    ]
});