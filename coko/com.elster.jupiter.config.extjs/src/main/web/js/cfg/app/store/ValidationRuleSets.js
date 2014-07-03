Ext.define('Cfg.store.ValidationRuleSets', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.ValidationRuleSet',
    pageSize: 10,

    storeId: 'ValidationRuleSets',

    listeners: {
        'beforeLoad': function () {
            var extraParams = this.proxy.extraParams;

            // replace filter extra params with new ones
            if (this.proxyFilter) {
                extraParams = _.omit(extraParams, this.proxyFilter.getFields());
                Ext.merge(extraParams, this.getFilterParams());
            }

            this.proxy.extraParams = extraParams;
        }
    },

    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        reader: {
            type: 'json',
            root: 'ruleSets',
            totalProperty: 'total'
        }
    }
});