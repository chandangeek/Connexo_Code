/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.AssignedToMeIssues', {
    extend: 'Ext.data.Model',
    requires: [
        'Dsh.model.TopMyIssue'
    ],

    fields: [
        { name: 'total', type: 'int' },
        { name: 'filter', type: 'auto' }
    ],
    hasMany: [
        {
            model: 'Dsh.model.TopMyIssue',
            name: 'topMyIssues'
        }
    ]
});