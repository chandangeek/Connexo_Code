/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.model.DeviceType', {
    extend: 'Ext.data.Model',
    fields: ['id', {
            name: 'name',
            convert: function (value, record) {
                return value;
            }
        }
    ]
});