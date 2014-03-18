Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Isu.component.filter.store.Filterable'
    ],

    mixins: [
        'Isu.component.filter.store.Filterable'
    ],

    model: 'Isu.model.Issues',
    pageSize: 10,
    autoLoad: false,

    listeners: {
        "beforeLoad": function() {
            var extraParams = this.proxy.extraParams;

            // replace filter extra params with new ones
            if (this.proxyFilter) {
                var data = this.getFilterParams();
                extraParams = _.omit(extraParams, this.proxyFilter.getFields());
                Ext.merge(extraParams, data);
            }

            this.proxy.extraParams = extraParams;
        }
    }
});