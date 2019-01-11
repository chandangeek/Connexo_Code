/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.store.Audit', {
    extend: 'Ext.data.Store',
    require: [
        'Mdc.audit.model.Audit'
    ],
    model: 'Mdc.audit.model.Audit',
    autoLoad: true,

    proxy: {
        type: 'rest',
        url: '/api/aud/audit',
        reader: {
            type: 'json',
            root: 'audit'
        }
    }
});
