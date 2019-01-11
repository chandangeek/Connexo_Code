/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.model.Audit', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'auto'},
        {name: 'operation', type: 'auto'},
        {name: 'changedOn', type: 'auto'},
        {name: 'category', type: 'auto'},
        {name: 'subCategory', type: 'auto'},
        {name: 'name', type: 'auto'},
        {name: 'auditLogs'},
        {name: 'user', type: 'auto'}
    ],
    requires: [
        'Mdc.audit.model.AuditLog'
    ],
    associations: [
        {
            name: 'auditLogs',
            type: 'hasMany',
            model: 'Mdc.audit.model.AuditLog',
            associationKey: 'auditLogs'
        }
    ]
});
