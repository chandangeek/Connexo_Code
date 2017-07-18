/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.container.EmptyGridContainer
 *
 * The {@link Uni.view.container.EmptyGridContainer} shows a custom component when the grid
 * it is displaying does not have any data. By default it shows the grid, after the store is
 * done loading with no items then the custom {@link #emptyComponent} will be shown.
 *
 * The {@link #grid} needs to be of the type {@link Ext.grid.Panel} and have a valid store
 * attached as property. While the {@link #emptyComponent} can be any type of component.
 *
 * # How to use
 *
 *     @example
 *     {
 *         xtype: 'emptygridcontainer',
 *         grid: {
 *             xtype: 'Ext.grid.Panel',
 *             store: 'myStore',
 *             // Other properties.
 *         },
 *         emptyComponent: {
 *             xtype: 'component',
 *             html: '<h4>There are no items</h4>'
 *         }
 *     }
 *
 */
Ext.define('Uni.view.container.EmptyGridContainer', {
    extend: 'Ext.container.Container',
    xtype: 'emptygridcontainer',

    layout: 'card',
    activeItem: 1,

    /**
     * @cfg {Object/Ext.grid.Panel}
     *
     * Grid to show in the panel
     */
    grid: null,

    /**
     * @cfg {Object/Ext.Component}
     *
     * Component to show when the grid store is empty after loading.
     */
    emptyComponent: null,

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    items: [
        {
            xtype: 'container',
            itemId: 'emptyContainer'
        },
        {
            xtype: 'container',
            itemId: 'gridContainer'
        }
    ],

    initComponent: function () {
        var me = this,
            grid = me.grid,
            emptyCmp = me.emptyComponent;

        if (!(grid instanceof Ext.Component)) {
            grid = Ext.clone(grid);
        }

        me.items[1].items = grid;

        if (!(emptyCmp instanceof Ext.Component)) {
            emptyCmp = Ext.clone(emptyCmp);
        }

        me.items[0].items = emptyCmp;

        this.callParent(arguments);

        me.grid = me.getGridCt().items.items[0];
        me.bindStore(me.grid.store || 'ext-empty-store', true);

        this.on('beforedestroy', this.onBeforeDestroy, this);
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            load: this.onLoad
        };
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    onBeforeLoad: function () {
        var me = this,
            activeIndex = me.items.indexOf(me.getLayout().getActiveItem());

        if (activeIndex !== 1) {
            me.getLayout().setActiveItem(1);
        }
    },

    onLoad: function (store, records, successful) {
        var me = this,
            isEmpty = Ext.isDefined(successful)
                ? !(successful && store.getCount() && store.getTotalCount())
                : !me.grid.store.getCount();

        me.getLayout().setActiveItem(isEmpty ? me.getEmptyCt() : me.getGridCt());
    },

    getGridCt: function () {
        return this.down('#gridContainer');
    },

    getEmptyCt: function () {
        return this.down('#emptyContainer');
    }
});