/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.CommandForRule', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'command'},
        {
            name: 'displayName',
            persist: false,
            mapping: function (data) {
                return (data.command && data.command.category && data.command.command)
                    ? data.command.category + ' - ' + data.command.command
                    : '?';
            }
        }
    ],
    associations: [
        {
            name: 'command',
            type: 'hasOne',
            model: 'Mdc.model.Command',
            associationKey: 'command',
            getterName: 'getCommand',
            setterName: 'setCommand',
            foreignKey: 'command'
        }
    ]
});