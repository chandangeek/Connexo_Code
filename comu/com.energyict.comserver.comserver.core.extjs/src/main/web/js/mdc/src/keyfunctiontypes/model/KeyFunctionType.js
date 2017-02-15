/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.keyfunctiontypes.model.KeyFunctionType', {
    extend: 'Uni.model.ParentVersion',
    requires: [
      'Mdc.keyfunctiontypes.model.KeyType'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'keyType', type: 'auto'},
        {name: 'validityPeriod', type: 'auto', useNull: true},
        {name: 'viewLevels', type: 'auto'},
        {name: 'editLevels', type: 'auto'},
        {name: 'defaultViewLevels', type: 'auto'},
        {name: 'defaultEditLevels', type: 'auto'},
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
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/keyfunctiontypes',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    },
    associations: [
        {
            name: 'keyType',
            type: 'hasOne',
            model: 'Mdc.keyfunctiontypes.model.KeyType',
            associationKey: 'keyType',
            getterName: 'getKeyType',
            setterName: 'setKeyType',
            foreignKey: 'keyType'
        }
    ]
});