/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.model.eventType.EndDeviceEventTypePart', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {   name: 'mnemonic',
            type: 'string'
        },
        {   name: 'value',
            type: 'int'
        },
        {   name: 'displayName',
            type: 'string'
        }
    ]
});