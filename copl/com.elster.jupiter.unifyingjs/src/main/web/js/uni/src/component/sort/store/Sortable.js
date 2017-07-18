/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.component.sort.store.Sortable
 *
 * Sortable store is a mixin that allow you to bind Sort model (See: {@Link Uni.component.sort.model.Sort})
 * to the store, and retrieve plain (ready for sending via configured proxy) data from this model;
 *
 * Class fires "updateProxySort" event with an sort model parameter.
 */
Ext.define('Uni.component.sort.store.Sortable', {

    proxySort: null,

    /**
     * @param sortModel Uni.component.filter.model.Filter
     */
    setProxySort: function(sortModel) {
        //<debug>
        if (!sortModel instanceof Uni.component.filter.model.Filter) {
            Ext.Error.raise('!sortModel instanceof Uni.component.filter.model.Filter');
        }
        //</debug>

        this.proxySort = sortModel;
        this.updateProxySort();
    },

    /*
     * @returns {Uni.component.filter.model.Filter}
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