/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.WebServiceEndPointModel', {
    extend: 'Ext.data.Model',

    idProperty: 'name',

    fields: [
        { name: 'id', type: 'int'},
        { name: 'name', type: 'string'}
    ]
});