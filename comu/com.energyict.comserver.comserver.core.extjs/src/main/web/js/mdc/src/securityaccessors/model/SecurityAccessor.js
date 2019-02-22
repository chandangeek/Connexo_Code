/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
var securityAccessorWritter = Ext.create('Ext.data.writer.Json', {
    getRecordData: function (record) {
        if (record.get('keyType').name != 'HSM Key') {
            delete record.data.importCapability;
            delete record.data.renewCapability;
            delete record.data.label;
            delete record.data.keySize;
            delete record.data.hsmJssKeyType;
            delete record.data.isReversible;
        }
        return record.data;
    }
});

Ext.define('Mdc.securityaccessors.model.SecurityAccessor', {
    extend: 'Uni.model.Version',
    requires: [
        'Mdc.securityaccessors.model.KeyType'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'description', type: 'string'},
        {name: 'keyType', type: 'auto'},
        {name: 'purpose', type: 'auto'},
        {name: 'storageMethod', type: 'auto'},
        {name: 'trustStoreId', type: 'int'},
        {name: 'duration', type: 'auto', useNull: true},
        {name: 'viewLevels', type: 'auto', useNull: true},
        {name: 'editLevels', type: 'auto', useNull: true},
        {name: 'defaultViewLevels', type: 'auto', useNull: true},
        {name: 'defaultEditLevels', type: 'auto', useNull: true},
        {name: 'defaultValue', type: 'auto', useNull: true, defaultValue: null},
        {name: 'hsmJssKeyType', type: 'auto', useNull: true, defaultValue: null},
        {name: 'label', type: 'auto'},
        {name: 'importCapability', type: 'auto'},
        {name: 'renewCapability', type: 'auto'},
        {name: 'keySize', type: 'int'},
        {name: 'isReversible', type: 'boolean', defaultValue: true, convert: null},

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
        {
            name: 'manageCentrally',
            persist: false,
            mapping: function (data) {
                return !!data.defaultValue;
            }
        },
        {
            name: 'activeCertificate',
            persist: false,
            mapping: function (data) {
                var defaultValue = data.defaultValue,
                    value = '';
                if(defaultValue && defaultValue.currentProperties){
                    _.map(defaultValue.currentProperties, function(property){
                        var key = property.key;
                        if (key === 'alias') {
                            value = property.propertyValueInfo.value;
                        }
                    });
                }
                return value;
            }
        },
        {
            name: 'passiveCertificate',
            persist: false,
            mapping: function (data) {
                var defaultValue = data.defaultValue,
                    value = '';
                if(defaultValue && defaultValue.tempProperties){
                    _.map(defaultValue.tempProperties, function(property){
                        var key = property.key;
                        if (key === 'alias') {
                            value = property.propertyValueInfo.value;
                        }
                    });
                }
                return value;
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/dtc/securityaccessors',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        },
        writer: securityAccessorWritter
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