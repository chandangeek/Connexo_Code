/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.TimeOfUses',{
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.field.TimeOfUse',
    storeId: 'timeOfUses',

    proxy: {
        type: 'rest',
        url: '../../api/dtc/field/timeOfUse',
        reader: {
            type: 'json',
            root: 'timeOfUse'
        }
    }
});