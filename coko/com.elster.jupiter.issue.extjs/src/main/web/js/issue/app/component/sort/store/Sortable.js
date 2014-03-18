Ext.define('Isu.component.sort.store.Sortable', {

    proxySort: null,

    /**
     * @param sortModel Isu.component.filter.model.Filter
     */
    setProxySort: function(sortModel) {
        if (!sortModel instanceof Isu.component.filter.model.Filter) {
            //<debug>
            Ext.Error.raise('!sortModel instanceof Isu.component.filter.model.Filter');
            //</debug>
        }

        this.proxySort = sortModel;
        this.updateProxySort();
    },

    /*
     * @returns {Isu.component.filter.model.Filter}
     */
    getProxySort: function() {
        return this.proxySort;
    },

    updateProxySort: function() {
        this.load();
        this.fireEvent('updateProxySort', this.proxySort);
    },

    getSortParams: function() {
        return this.proxySort.getPlainData();
    }
});