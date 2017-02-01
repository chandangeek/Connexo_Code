/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.keyfunctiontypes.store.KeyFunctionTypes', {
    extend: 'Ext.data.Store',
    storeId: 'keyFunctionTypesStore',
    requires: [
        'Mdc.keyfunctiontypes.model.KeyFunctionType'
    ],
    model: 'Mdc.keyfunctiontypes.model.KeyFunctionType',
    autoLoad: false,
    //proxy: {
    //    type: 'rest',
    //    urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/keyfunctiontypes',
    //    reader: {
    //        type: 'json',
    //        root: 'data'
    //    },
    //    pageParam: false,
    //    startParam: false,
    //    limitParam: false,
    //
    //    setUrl: function(deviceTypeId) {
    //        this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
    //    }
    //}
    data: {
        total: 11,
        keyfunctiontypes: [
            {
                id: 2,
                name: "AK-RW",
                validityPeriod: {
                    count: 5, timeUnit: "minutes", localizedTimeUnit: "minutes", asSeconds: 300
                },
                keyType: "Symmetric key",
                description: "This is the description of the key function type"
            },
            {
                id: 1002,
                name: "AK-RW2",
                validityPeriod: {
                    count: 6, timeUnit: "seconds", localizedTimeUnit: "seconds", asSeconds: 6
                },
                keyType: "Symmetric key",
                description: "This is the description of the key function type"
            }
        ]
    },
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'keyfunctiontypes'
        }
    },

    associations: [
        {
            instanceName: 'keyType',
            name: 'keyType',
            type: 'hasOne',
            model: 'Mdc.keyfunctiontypes.model.KeyType',
            associationKey: 'keyType',
            foreignKey: 'keyType',
            getTypeDiscriminator: function (node) {
                return 'Mdc.keyfunctiontypes.model.KeyType';
            }
        }
    ]
})
;