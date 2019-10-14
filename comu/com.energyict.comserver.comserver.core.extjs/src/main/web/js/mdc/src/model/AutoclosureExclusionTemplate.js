/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.AutoclosureExclusionTemplate', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'displayName',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        }
    ]
});
