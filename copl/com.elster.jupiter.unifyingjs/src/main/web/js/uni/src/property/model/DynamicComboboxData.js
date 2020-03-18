/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.property.model.DynamicComboboxData', {
    extend: 'Ext.data.Model',

    fields: [
        { name: 'name', type: 'auto' },
        { name: 'value',
         mapping: function (data) {
             return data.value !== null && data.value !== undefined ? data.value : data.name;
        }}
    ]
});
