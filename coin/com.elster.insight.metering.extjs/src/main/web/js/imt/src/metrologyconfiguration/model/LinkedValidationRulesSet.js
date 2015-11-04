Ext.define('Imt.metrologyconfiguration.model.LinkableValidationRulesSet', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'ruleSets', type: 'auto'},

    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/ucr/metrologyconfigurations/{id}/assignablevalidationrulesets',
        timeout: 240000,
        reader: {
            type: 'json',
 //           root: 'assignedvalidationrulesets'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', encodeURIComponent(params));
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});
