/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.ValidationActions', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.ValidationAction',
    storeId: 'validationActions',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/val/validation/actions',
        reader: {
            type: 'json',
            root: 'actions'
        }
    }
});
