/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.model.Location', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
//        {name: 'id', type: 'string'},   // uses string to comply with Isu.model.Group
        {name: 'name', type: 'string'}
    ],
    idProperty: 'name',

    proxy: {
        type: 'rest',
        url: '/api/isu/locations',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});