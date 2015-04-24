Ext.define('Est.estimationrulesets.store.EstimationRuleSetsStore', {
    extend: 'Ext.data.Store',
    model: 'Est.estimationrulesets.model.EstimationRuleSet',
    proxy: {
        url: '../../api/est/estimation',
        type: 'rest',
        reader: {
            type: 'json',
            root: 'ruleSets',
            totalProperty: 'total'
        }
    }
});