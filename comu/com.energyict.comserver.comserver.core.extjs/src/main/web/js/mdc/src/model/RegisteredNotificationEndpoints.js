/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.RegisteredNotificationEndpoints', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'id', type: 'int'},
        {name:'name', type: 'string'},
        {name: 'version', type: 'string'}
    ]
});