/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.EstimationRuleSet', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id'
        },
        {
            name: 'name', type: 'string',
            useNull: true
        },
        {
            name: 'inactiveRules',
            type: 'int'
        },
        {
            name: 'activeRules',
            type: 'int'
        },
        {
            name: 'description',
            type: 'string'
        },
        {
            name: 'isActive',
            type: 'boolean'
        }
    ],

    proxy: {
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/estimationrulesets',
        timeout: 60000,
        type: 'rest',
        reader: {
            type: 'json',
            root: 'rulesets'
        }
    }
});
