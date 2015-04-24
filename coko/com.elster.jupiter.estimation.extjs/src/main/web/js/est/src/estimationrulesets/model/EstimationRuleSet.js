Ext.define('Est.estimationrulesets.model.EstimationRuleSet', {
    extend: 'Ext.data.Model',
    requires: [
        'Est.estimationrules.model.Rule'
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
            model: 'Est.estimationrules.model.Rule',
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