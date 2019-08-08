/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.audit.model.AuditReference', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'name', type: 'auto'},
        {name: 'reference', type: 'auto'},
        {name: 'removed', type: 'auto'}
    ]
});