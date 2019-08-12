/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.IssueDevice', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    fields: [
        {
            name: 'id',
            type: 'auto'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'mRID',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices',
        reader: {
            type: 'json',
            root: 'devices'
        }
    }
});