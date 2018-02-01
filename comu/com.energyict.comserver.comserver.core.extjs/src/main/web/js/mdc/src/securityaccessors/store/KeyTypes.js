/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.securityaccessors.store.KeyTypes', {
    extend: 'Ext.data.Store',
    storeId: 'keyTypesStore',
    requires: [
        'Mdc.securityaccessors.model.KeyType'
    ],
    model: 'Mdc.securityaccessors.model.KeyType',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dtc/securityaccessors/keytypes',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});