/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ConflictItem', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'}
    ]
});