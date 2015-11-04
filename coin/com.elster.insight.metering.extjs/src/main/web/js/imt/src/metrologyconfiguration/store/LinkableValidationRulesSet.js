Ext.define('Imt.metrologyconfiguration.store.LinkableValidationRulesSet', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.ValidationRuleSet',
//	data : [
//	    	{'id': '1', 'name': 'rule-set-1'},
//	       	{'id': '3', 'name': 'rule-set-3'},
//	    	{'id': '5', 'name': 'rule-set-5'},
//	]
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