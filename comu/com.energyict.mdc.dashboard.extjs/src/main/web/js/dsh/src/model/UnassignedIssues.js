/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.UnassignedIssues', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'total', type: 'int' },
        { name: 'filter', type: 'auto' }
    ]
});