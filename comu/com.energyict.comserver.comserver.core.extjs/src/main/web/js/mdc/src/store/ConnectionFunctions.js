/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ConnectionFunctions', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ConnectionFunction'
    ],
    model: 'Mdc.model.ConnectionFunction',
    storeId: 'connectionFunctions',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/connectionFunctions',
        reader: {
            type: 'json',
            root: 'connectionFunctions'
        }
    }
});