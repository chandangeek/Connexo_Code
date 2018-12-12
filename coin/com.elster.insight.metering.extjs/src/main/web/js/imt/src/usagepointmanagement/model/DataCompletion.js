/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.DataCompletion', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'statistics', type: 'auto', useNull: true},        
        {name: 'total', type: 'int'}
    ]
});