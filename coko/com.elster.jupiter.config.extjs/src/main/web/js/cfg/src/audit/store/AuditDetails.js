/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.audit.store.AuditDetails', {
    extend: 'Ext.data.Store',
    require: [
        'Cfg.audit.model.AuditLog'
    ],
    model: 'Cfg.audit.model.AuditLog',
    autoLoad: true,

    proxy: {
        type: 'memory',
        reader: {
            type: 'json'
        }
    },
    data: []
});
