/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.WrappingSecurityAccessors', {
    extend: 'Ext.data.Store',
    storeId: 'SecurityAccessorsStore',
    requires: [
        'Mdc.securityaccessors.model.SecurityAccessor'
    ],
    model: 'Mdc.securityaccessors.model.SecurityAccessor',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/securityaccessors/{securityAccessorId}/wrappers',
        reader: {
            type: 'json',
            root: 'securityaccessors'
        },

        setUrl: function(deviceTypeId, securityAccessorId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId).replace('{securityAccessorId}', securityAccessorId);
        }
    },

    associations: [
        {
            instanceName: 'keyType',
            name: 'keyType',
            type: 'hasOne',
            model: 'Mdc.securityaccessors.model.KeyType',
            associationKey: 'keyType',
            foreignKey: 'keyType',
            getTypeDiscriminator: function (node) {
                return 'Mdc.securityaccessors.model.KeyType';
            }
        }
    ]
});