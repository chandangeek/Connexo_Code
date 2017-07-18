/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.model.TimeOfUseOption', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string',
            useNull: true
        },
        {
            name: 'name',
            type: 'string',
            useNull: true
        }
    ]

});
