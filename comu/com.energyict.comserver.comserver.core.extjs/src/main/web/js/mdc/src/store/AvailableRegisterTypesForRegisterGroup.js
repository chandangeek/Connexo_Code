/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableRegisterTypesForRegisterGroup', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'AvailableRegisterTypesForRegisterGroup',
    buffered: true,
    pageSize: 100,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/registergroups/{registerGroup}/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes',
            totalProperty: 'total'
        }
    }
});