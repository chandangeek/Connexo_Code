/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.audit.store.Audit', {
    extend: 'Ext.data.Store',
    require: [
        'Cfg.audit.model.Audit'
    ],
    model: 'Cfg.audit.model.Audit',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/aud/audit',
        reader: {
            type: 'json',
            root: 'audit'
        }
    }
});
