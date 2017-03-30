/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.model.performance.Storage', {
    extend: 'Ext.data.Model',
    fields: [
        'time', 'load', 'threads', 'priority', 'capacity'
    ]
});