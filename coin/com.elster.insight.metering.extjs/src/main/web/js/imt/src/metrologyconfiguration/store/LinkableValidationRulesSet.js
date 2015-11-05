Ext.define('Imt.metrologyconfiguration.store.LinkableValidationRulesSet', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.ValidationRuleSet',

    proxy: {
        type: 'rest',
        urlTpl: '/api/ucr/metrologyconfigurations/{id}/assignablevalidationrulesets',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'assignablevalidationrulesets'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', encodeURIComponent(params));
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});