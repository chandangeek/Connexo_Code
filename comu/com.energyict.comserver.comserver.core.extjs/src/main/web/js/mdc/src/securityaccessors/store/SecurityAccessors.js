/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.store.SecurityAccessors', {
    extend: 'Ext.data.Store',
    storeId: 'SecurityAccessorsStore',
    requires: [
        'Mdc.securityaccessors.model.SecurityAccessor'
    ],
    model: 'Mdc.securityaccessors.model.SecurityAccessor',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/securityaccessors',
        reader: {
            type: 'json',
            root: 'securityaccessors'
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
            model: 'Mdc.securityaccessors.model.KeyType',
            associationKey: 'keyType',
            foreignKey: 'keyType',
            getTypeDiscriminator: function (node) {
                return 'Mdc.securityaccessors.model.KeyType';
            }
        }
    ]
});