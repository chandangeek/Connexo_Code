/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ComPortTypes',{
    extend: 'Ext.data.Store',
    fields: ['id', 'localizedValue'],
    storeId: 'comporttypes',
    proxy: {
        type: 'rest',
        url: '../../api/mdc/field/comPortType',
        reader: {
            type: 'json',
            root: 'comPortTypes'
        }
    }
});