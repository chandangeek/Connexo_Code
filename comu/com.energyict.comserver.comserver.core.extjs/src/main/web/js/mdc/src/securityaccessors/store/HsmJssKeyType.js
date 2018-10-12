/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.securityaccessors.store.HsmJssKeyTypes', {
    extend: 'Ext.data.Store',
    storeId: 'hsmJssKeyTypes',
    autoLoad: false,
    fields: [{
        name: 'name',
        mapping: function (data) {
            return data;
        }
    }],
    proxy: {
        type: 'rest',
        url: '/api/dtc/securityaccessors/hsm/keyType',
        reader: {
            type: 'array'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});