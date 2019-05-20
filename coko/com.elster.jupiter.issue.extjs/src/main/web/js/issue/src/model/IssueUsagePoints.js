/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.IssueUsagePoints', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'serialNumber', type: 'string'},
        {name: 'usagePoint', type: 'auto'},
        {name: 'serviceLocation', type: 'auto'},
        {name: 'serviceCategory', type: 'auto'},
        {name: 'version', type: 'int'}
    ],
    idProperty: 'name',

    proxy: {
        type: 'rest',
        url: '/api/isu/usagepoints',
        reader: {
            type: 'json',
            root: 'usagePoints'
        }
    }
});