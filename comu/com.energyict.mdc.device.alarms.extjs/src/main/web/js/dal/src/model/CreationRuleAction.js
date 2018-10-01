/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.model.CreationRuleAction', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property',
        'Dal.model.Action'
    ],
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'phase',
            type: 'auto'
        },
        {
            name: 'description',
            type: 'auto'
        }
    ],
    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
        },
        {
            type: 'hasOne',
            model: 'Dal.model.Action',
            associatedName: 'type',
            associationKey: 'type',
            getterName: 'getType',
            setterName: 'setType'
        }
    ]
});