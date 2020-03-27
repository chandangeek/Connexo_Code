/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.securityaccessors.store.KeyPurposes', {
    extend: 'Ext.data.Store',
    storeId: 'keyPurposesStore',
    requires: [
        'Mdc.securityaccessors.model.KeyPurpose'
    ],
    model: 'Mdc.securityaccessors.model.KeyPurpose',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dtc/securityaccessors/keypurposes',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});