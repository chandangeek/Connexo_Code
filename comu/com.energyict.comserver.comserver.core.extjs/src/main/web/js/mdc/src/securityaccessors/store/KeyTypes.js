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
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/securityaccessors/keytypes',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});