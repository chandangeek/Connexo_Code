/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.model.status.Pool', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            sortType: 'asUCText' // To make the sorting case-insensitive
        },
        'description', 'inbound', 'active'
    ]
});