/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Ldr.model.Translation
 */
Ext.define('Ldr.model.Translation', {
    extend: 'Ext.data.Model',
    fields: [
        'cmp',
        'key',
        'value',
        {
            name: 'id',
            mapping: function (data) {
                return data.key + ':' + data.cmp
            }
        }
    ]
});