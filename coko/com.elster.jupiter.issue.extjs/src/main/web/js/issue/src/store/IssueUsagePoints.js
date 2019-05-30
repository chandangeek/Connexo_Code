/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.store.IssueUsagePoints', {
    extend: 'Ext.data.Store',
    fields: [
        'id',
        'name'
    ],
    pageSize: 50,
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints',
        start: 0,
        limit: 50,
        reader: {
            type: 'json',
            root: 'usagePoints'
        }
    }

});