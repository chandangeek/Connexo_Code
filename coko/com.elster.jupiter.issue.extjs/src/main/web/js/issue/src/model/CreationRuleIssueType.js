/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.CreationRuleIssueType', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uid',
            type: 'text'
        },
        {
            name: 'name',
            type: 'text'
        }
    ],

    idProperty: 'uid',

    proxy: {
        type: 'rest',
        url: '/api/isu/issuetypes/notmanual',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});