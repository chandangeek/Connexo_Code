/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.model.IssueStatus', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'auto'},
        {name: 'name', type: 'string'},
        {name: 'allowForClosing', type: 'boolean'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/itk/statuses',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});