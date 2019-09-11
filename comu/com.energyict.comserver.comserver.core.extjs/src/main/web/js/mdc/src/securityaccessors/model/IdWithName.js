/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.model.IdWithName', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'number'
        },
        {
            name: 'name',
            type: 'string'
        }
    ]
});