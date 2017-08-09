/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.model.CommandForDeviceGroup', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name:'category',
            type: 'string'
        },
        {
            name:'command',
            type: 'string'
        },
        {
            name: 'commandName',
            type: 'string'
        }
    ]
});