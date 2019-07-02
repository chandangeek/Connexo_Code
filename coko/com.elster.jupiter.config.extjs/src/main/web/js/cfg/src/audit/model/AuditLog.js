/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.audit.model.AuditLog', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'name', type: 'auto'},
        {name: 'value', type: 'auto'},
        {name: 'previousValue', type: 'auto'},
        {name: 'type', type: 'auto'}
    ]
});