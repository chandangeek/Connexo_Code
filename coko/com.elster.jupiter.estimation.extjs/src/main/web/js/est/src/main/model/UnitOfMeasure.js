/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.main.model.UnitOfMeasure', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'multiplier', type: 'integer', useNull: true},
        {name: 'unit', type: 'integer', useNull: true}
    ]
});