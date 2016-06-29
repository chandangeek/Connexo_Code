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
