/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.store.AuditDetails', {
    extend: 'Ext.data.Store',
    require: [
        'Mdc.audit.model.AuditLog'
    ],
    model: 'Mdc.audit.model.AuditLog',
    autoLoad: true,

    proxy: {
        type: 'memory',
        reader: {
            type: 'json'
        }
    },
    data: []
});
