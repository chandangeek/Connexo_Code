/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.main.model.Interval', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'time', type: 'integer', useNull: true},
        {name: 'macro', type: 'integer', useNull: true}
    ]
});