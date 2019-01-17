/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.store.Audit', {
    extend: 'Ext.data.Store',
    require: [
        'Mdc.audit.model.Audit'
    ],
    model: 'Mdc.audit.model.Audit',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/aud/audit/trail',
        reader: {
            type: 'json',
            root: 'audit'
        }
    }
});
