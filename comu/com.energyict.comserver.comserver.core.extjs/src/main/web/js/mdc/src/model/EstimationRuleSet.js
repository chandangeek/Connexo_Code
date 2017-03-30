/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.EstimationRuleSet', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.EstimationRule'
    ],
    fields: [
        {
            name: 'id',
            persist: false
        },
        {
            name: 'name', type: 'string',
            useNull: true
        },
        {
            name: 'numberOfInactiveRules',
            type: 'int',
            persist: false
        },
        {
            name: 'numberOfRules',
            type: 'int',
            persist: false
        },
        {
            name: 'numberOfActiveRules',
            persist: false,
            mapping: function (data) {
                return data.numberOfRules - data.numberOfInactiveRules;
            }
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            persist: false,
            name: 'implementation',
            type: 'string'
        }
    ],

    associations: [
        {
            name: 'rules',
            type: 'hasMany',
            model: 'Mdc.model.EstimationRule',
            associationKey: 'rules',
            foreignKey: 'rules'
        }
    ],

    proxy: {
        url: '../../api/est/estimation',
        type: 'rest',
        reader: {
            type: 'json'
        }
    }
});