/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.model.Audit', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'auto'},
        {name: 'operation', type: 'auto'},
        {name: 'operationType', type: 'auto'},
        {name: 'changedOn', type: 'auto'},
        {name: 'domain', type: 'auto'},
        {name: 'context', type: 'auto'},
        {name: 'domainType', type: 'auto'},
        {name: 'contextType', type: 'auto'},
        {name: 'auditReference', type: 'auto'},
        {name: 'user', type: 'auto'},
        {name: 'auditLogs'},
        {name: 'auditReference'}
    ],
    requires: [
        'Mdc.audit.model.AuditLog',
        'Mdc.audit.model.AuditReference'
    ],
    associations: [
        {
            name: 'auditLogs',
            type: 'hasMany',
            model: 'Mdc.audit.model.AuditLog',
            associationKey: 'auditLogs'
        },
        {
            name: 'auditReference',
            type: 'hasOne',
            model: 'Mdc.audit.model.AuditReference',
            associationKey: 'auditReference'
        }
    ]
});
