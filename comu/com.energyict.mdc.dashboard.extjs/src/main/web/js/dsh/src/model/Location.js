/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Dsh.model.Location', {
    extend: 'Ext.data.Model',
    fields: [
       {name: 'name', type: 'string'} ,
       {name: 'id', type: 'int'}
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/locations',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});