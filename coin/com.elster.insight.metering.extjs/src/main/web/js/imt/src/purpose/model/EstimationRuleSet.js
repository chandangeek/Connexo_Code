/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.EstimationRuleSet', {
    extend: 'Ext.data.Model',
    //extend: 'Uni.model.Version',
    // requires: [
    //     'Est.estimationrules.model.Rule'
    // ],
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
            //persist: false
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
            type: 'string'
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
