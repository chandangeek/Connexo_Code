/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.model.CommandStatus', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name:'deviceMessageStatus',
            type: 'string'
        },
        {
            name:'localizedValue',
            type: 'string'
        }
    ]
});