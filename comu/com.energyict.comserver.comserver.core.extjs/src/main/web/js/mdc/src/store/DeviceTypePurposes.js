/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceTypePurposes', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    requires: [
        'Mdc.model.field.DeviceTypePurpose'
    ],
    model: 'Mdc.model.field.DeviceTypePurpose',
    storeId: 'DeviceTypePurposes',
    sorters: [{
        property: 'localizedValue',
        direction: 'ASC'
    }],
    proxy: {
        type: 'rest',
        limitParam: false,
        pageParam: false,
        startParam: false,
        url: '../../api/dtc/field/deviceTypePurpose',
        reader: {
            type: 'json',
            root: 'deviceTypePurpose'
        }
    }

});