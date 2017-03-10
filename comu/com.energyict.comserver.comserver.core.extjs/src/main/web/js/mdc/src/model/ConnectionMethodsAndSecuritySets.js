/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ConnectionMethodsAndSecuritySets', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ConflictItem'
    ],
    fields: [
        {name : "from", type: "auto"}
    ],
    associations: [
        {
            name: 'to',
            type: 'hasMany',
            model: 'Mdc.model.ConflictItem',
            associationKey: 'to',
            foreignKey: 'to'
        }
    ]
});