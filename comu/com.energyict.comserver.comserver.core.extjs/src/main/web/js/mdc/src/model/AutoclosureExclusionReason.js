/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.AutoclosureExclusionReason', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'id',
            type: 'auto'
        },
        {
            name: 'name',
            type: 'string'
        }
    ]
});