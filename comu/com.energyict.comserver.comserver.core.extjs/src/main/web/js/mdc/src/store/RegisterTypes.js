/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'RegisterTypes',
    proxy: {
        type: 'rest',
        url: '../../api/mds/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});
