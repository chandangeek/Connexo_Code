/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Extend from this store if you want to use model for filtering
 */
Ext.define('Uni.data.store.Filterable', {
    extend: 'Ext.data.Store',

    remoteFilter: true,
    hydrator: null,

    /**
     * Initialises filters from filter model
     * @param config
     */
    constructor: function (config) {
        var me = this;

        config = config || {};

        me.callParent(arguments);

        var router = me.router = config.router || Uni.util.History.getRouterController();

        if (me.hydrator && Ext.isString(me.hydrator)) {
            me.hydrator = Ext.create(me.hydrator);
        }

        router.on('routematch', function () {
            if (router.filter) {
                me.setFilterModel(router.filter);
            }
        });
    },

    /**
     * returns data in a format of filter:
     * [{property: key, value: item}]
     */
    setFilterModel: function (model) {
        var me = this,
            data = me.hydrator ? me.hydrator.extract(model) : model.getData(),
            filters = [];

        _.map(data, function (item, key) {
            if (item) {
                filters.push({property: key, value: item});
            }
        });

        me.clearFilter(true);
        me.addFilter(filters, false);
    }
});

