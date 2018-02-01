/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.securityaccessors.store.KeyEncryptionMethods', {
    extend: 'Ext.data.Store',
    storeId: 'keyEncryptionMethods',
    requires: [
        'Mdc.securityaccessors.model.KeyEncryptionMethod'
    ],
    model: 'Mdc.securityaccessors.model.KeyEncryptionMethod',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/securityaccessors/keytypes/{keyTypeId}/keyencryptionmethods',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(keyTypeId) {
            this.url = this.urlTpl.replace('{keyTypeId}', keyTypeId);
        }
    }
});