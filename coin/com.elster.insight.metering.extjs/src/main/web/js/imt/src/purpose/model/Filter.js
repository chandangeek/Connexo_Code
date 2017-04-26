/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.Filter', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'name', type: 'auto', useNull: true},
        {name: 'id', type: 'auto', useNull: true},
        {
            name: 'display',
            type: 'auto',
            useNull: true,
            mapping: function (data) {
                return data.name;
            }
        },
        {
            name: 'value',
            type: 'auto',
            useNull: true,
            mapping: function (data) {
                return data.id;
            }
        },
    ]
});