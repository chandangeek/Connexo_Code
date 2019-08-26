/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.IssueUsagePoints', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'auto'}
    ],
    idProperty: 'name',

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints',
        reader: {
            type: 'json',
            root: "usagePoints"
        }
    }
});