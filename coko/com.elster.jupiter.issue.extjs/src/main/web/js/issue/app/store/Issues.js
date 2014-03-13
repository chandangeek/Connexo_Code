Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    model: 'Isu.model.Issues',
    pageSize: 10,
    autoLoad: false,
    proxyFilter: null,

    /**
     * @param filter Isu.component.filter.model.Filter
     */
    setProxyFilter: function(filter) {
        if (!filter instanceof Isu.component.filter.model.Filter) {
            //<debug>
            Ext.Error.raise('!filter instanceof Isu.component.filter.model.Filter');
            //</debug>
        }

        this.proxyFilter = filter;
        this.load();
        this.fireEvent('updateProxyFilter', this.proxyFilter);
    },

    /*
     * @returns {Isu.component.filter.model.Filter}
     */
    getProxyFilter: function() {
        return this.proxyFilter;
    },

    removeProxyFilter: function(key, id) {
        if (!key) {
            this.proxyFilter = null;
        } else {
            if (id) {
                var store = this.proxyFilter[key]();
                var rec = store.getById(id);
                if (rec) {
                    store.remove(rec);
                }
            } else if (!_.isUndefined(this.proxyFilter.data[key])){
                delete this.proxyFilter.data[key];
            }
        }
        this.load();
        this.fireEvent('updateProxyFilter', this.proxyFilter);
    },

    listeners: {
        "beforeLoad": function() {
            var extraParams = {};

            if (this.proxyFilter) {
                Ext.merge(extraParams, this.proxyFilter.getPlainData());
            }
            console.log(extraParams);
            Ext.merge(this.proxy.extraParams, extraParams);
        }
    }
});