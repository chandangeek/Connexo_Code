/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.model.performance.Pool', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'delay', // in ms
        'threads',
        'ports'
    ]
});