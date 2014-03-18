Ext.define('Isu.component.filter.store.Filterable', {

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
        this.updateProxyFilter();
    },

    /*
     * @returns {Isu.component.filter.model.Filter}
     */
    getProxyFilter: function() {
        return this.proxyFilter;
    },

    updateProxyFilter: function() {
        this.load();
        this.fireEvent('updateProxyFilter', this.proxyFilter);
    },

    getFilterParams: function() {
        return this.proxyFilter.getPlainData();
    }
});