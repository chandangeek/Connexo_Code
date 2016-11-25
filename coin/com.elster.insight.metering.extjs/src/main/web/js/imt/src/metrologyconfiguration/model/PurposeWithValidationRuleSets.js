Ext.define('Imt.metrologyconfiguration.model.PurposeWithValidationRuleSets', {
    extend: 'Uni.model.Version',
    requires: [
        'Imt.metrologyconfiguration.model.ValidationRuleSet'
    ],
    fields: [
        'id', 'name', 'mandatory'
    ],
    associations: [
        {
            name: 'validationRuleSets',
            type: 'hasMany',
            model: 'Imt.metrologyconfiguration.model.ValidationRuleSet',
            associationKey: 'validationRuleSets'
        },
        {
            name: 'validationTasks',
            type: 'hasMany',
            model: 'Imt.metrologyconfiguration.model.ValidationTask',
            associationKey: 'validationTasks'
        },
        {
            name: 'estimationRuleSets',
            type: 'hasMany',
            model: 'Imt.metrologyconfiguration.model.EstimationRuleSet',
            associationKey: 'estimationRuleSets'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ucr/metrologyconfigurations/{metrologyConfigurationId}/contracts',
        reader: {
            type: 'json'
        }        
    }    
});
