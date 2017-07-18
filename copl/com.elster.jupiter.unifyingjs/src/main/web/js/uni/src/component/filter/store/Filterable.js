/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.component.filter.store.Filterable
 * @deprecated
 *
 * Filterable store is a mixin that allow you to bind Filter model (See: {@Link Uni.component.filter.model.Filter})
 * to the store, and retrieve plain (ready for sending via configured proxy) data from this model;
 *
 * Class fires "updateProxyFilter" event with an filter model parameter.
 */
Ext.define('Uni.component.filter.store.Filterable', {

    proxyFilter: null,

    /**
     * @param filter Uni.component.filter.model.Filter
     */
    setProxyFilter: function(filter) {
        //<debug>
        if (!filter instanceof Uni.component.filter.model.Filter) {
            Ext.Error.raise('!filter instanceof Uni.component.filter.model.Filter');
        }
        //</debug>

        this.proxyFilter = filter;
        this.updateProxyFilter();
    },

    /*
     * @returns {Uni.component.filter.model.Filter}
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