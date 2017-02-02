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
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/keyfunctiontypes',
        reader: {
            type: 'json',
            root: 'keyfunctiontypes'
        },

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
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