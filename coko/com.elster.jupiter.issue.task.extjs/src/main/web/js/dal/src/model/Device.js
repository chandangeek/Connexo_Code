/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.model.Device', {
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
        url: '/api/itk/meters',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});