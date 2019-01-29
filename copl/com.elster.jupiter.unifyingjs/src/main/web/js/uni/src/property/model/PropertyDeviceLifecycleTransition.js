/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.model.PropertyDeviceLifecycleTransition', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'value',
            type: 'string'
        },
        {
            name: 'deviceLifecycleName',
            type: 'string'
        },
        {
            name: 'stateTransitionName',
            type: 'string'
        },
        {
            name: 'fromStateName',
            type: 'string'
        },
        {
            name: 'toStateName',
            type: 'string'
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            name: 'id',
            type: 'string'
        }
    ],

    idProperty: 'name',

    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/rules/templates',
        reader: {
            type: 'json',
            root: 'creationRuleTemplates'
        }
    }
});