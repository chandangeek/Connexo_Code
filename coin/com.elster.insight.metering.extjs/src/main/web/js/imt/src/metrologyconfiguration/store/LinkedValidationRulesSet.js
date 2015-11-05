Ext.define('Imt.metrologyconfiguration.store.LinkedValidationRulesSet', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.ValidationRuleSet',
    
    proxy: {
        type: 'rest',
        urlTpl: '/api/ucr/metrologyconfigurations/{id}/assignedvalidationrulesets',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'assignedvalidationrulesets'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', encodeURIComponent(params));
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});