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
        url: '/api/dtc/devicetypes/{deviceTypeId}/keyfunctiontypes', //hier iets van keytypes
        reader: {
            type: 'json',
            root: 'data'
        }
    }
})
;