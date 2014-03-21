/**
 * @class Isu.component.filter.store.Filterable
 *
 * Filterable store is a mixin that allow you to bind Filter model (See: {@Link Isu.component.filter.model.Filter})
 * to the store, and retrieve plain (ready for sending via configured proxy) data from this model;
 *
 * Class fires "updateProxyFilter" event with an filter model parameter.
 */
Ext.define('Isu.component.filter.store.Filterable', {

    proxyFilter: null,

    /**
     * @param filter Isu.component.filter.model.Filter
     */
    setProxyFilter: function(filter) {
        //<debug>
        if (!filter instanceof Isu.component.filter.model.Filter) {
            Ext.Error.raise('!filter instanceof Isu.component.filter.model.Filter');
        }
        //</debug>

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