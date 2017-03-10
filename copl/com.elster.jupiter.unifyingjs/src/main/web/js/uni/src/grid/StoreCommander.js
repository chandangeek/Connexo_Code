/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.StoreCommander
 */
Ext.define('Uni.grid.StoreCommander', {
    extend: 'Ext.container.Container',
    xtype: 'uni-grid-storecommander',

    requires: [
        'Uni.grid.commander.FilteringPanel',
        'Uni.grid.commander.GroupingPanel',
        'Uni.grid.commander.SortingPanel'
    ],

    /**
     * @cfg {Ext.data.Store}
     */
    store: new Ext.data.ArrayStore(),

    /**
     * @cfg {Boolean} filteringEnabled
     */
    filteringEnabled: true,

    /**
     * @cfg {Boolean} groupingEnabled
     */
    groupingEnabled: true,

    /**
     * @cfg {Boolean} sortingEnabled
     */
    sortingEnabled: true,

    items: [],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.store = Ext.getStore(me.store) || Ext.create(me.store);
        me.initPanels();
    },

    initPanels: function () {
        var me = this;

        if (me.filteringEnabled) {
            me.add({
                xtype: 'uni-grid-commander-filteringpanel',
                store: me.store
            });
        }

        if (me.groupingEnabled) {
            me.add({
                xtype: 'uni-grid-commander-groupingpanel',
                store: me.store
            });
        }

        if (me.sortingEnabled) {
            me.add({
                xtype: 'uni-grid-commander-sortingpanel',
                store: me.store
            });
        }
    },

    reconfigureStore: function (store) {
        var me = this;

        if (Ext.isDefined(store)) {
            if (me.filteringEnabled) {
                me.getFilteringPanel().reconfigureStore(store);
            }

            if (me.groupingEnabled) {
                me.getGroupingPanel().reconfigureStore(store);
            }

            if (me.sortingEnabled) {
                me.getSortingPanel().reconfigureStore(store);
            }
        }
    },

    getFilteringPanel: function () {
        return this.down('uni-grid-commander-filteringpanel');
    },

    getGroupingPanel: function () {
        return this.down('uni-grid-commander-groupingpanel');
    },

    getSortingPanel: function () {
        return this.down('uni-grid-commander-sortingpanel');
    }
});