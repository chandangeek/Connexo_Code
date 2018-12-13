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
        url: '/api/dtc/securityaccessors',
        reader: {
            type: 'json',
            root: 'securityaccessors'
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