/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.BulkChangeIssues', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'operation', type: 'string'},
        {name: 'status', type: 'string'},
        {name: 'comment', type: 'string'},
        {name: 'assignee', type: 'auto'}
    ],

    hasMany: {model: 'Isu.model.BulkIssues', name: 'issues'}
});
