/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'description',
            type: 'text'
        },
        {
            name: 'number',
            type: 'int'
        },
        {
            name: 'href',
            persist: false
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/issues/groupedlist',
        reader: {
            type: 'json',
            root: 'issueGroups'
        }
    }
});