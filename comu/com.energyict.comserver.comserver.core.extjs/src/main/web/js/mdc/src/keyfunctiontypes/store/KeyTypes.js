/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.keyfunctiontypes.store.KeyTypes', {
    extend: 'Ext.data.Store',
    storeId: 'keyTypesStore',
    requires: [
        'Mdc.keyfunctiontypes.model.KeyType'
    ],
    model: 'Mdc.keyfunctiontypes.model.KeyType',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/keyfunctiontypes/keytypes',
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
})
;