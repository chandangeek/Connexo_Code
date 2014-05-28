/**
 * @class Uni.view.container.PreviewContainer
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
 * @Example
 *
 *     {
 *         xtype: 'preview-container',
 *         grid: {
 *             xtype: 'Ext.grid.Panel',
 *             store: 'myStore',
 *             // Other properties.
 *         },
 *         emptyComponent: {
 *             xtype: 'component',
 *             html: 'There are no items'
 *         },
 *         previewComponent: {
 *             xtype: 'component',
 *             html: 'Some preview.'
 *         }
 *     }
 *
 */
Ext.define('Uni.view.container.PreviewContainer', {
    extend: 'Ext.container.Container',
    xtype: 'preview-container',

    layout: 'card',

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

    /**
     * @cfg {Object/Ext.Component}
     *
     * Component to show the preview in of the selected row. This is hidden when there are no rows.
     */
    previewComponent: null,

    mixins: {
        bindable: 'Ext.util.Bindable'
    },

    items: [
        {
            xtype: 'container'
        },
        {
            xtype: 'container',
            itemId: 'wrapper-container',
            items: [
            ]
        }
    ],

    initComponent: function () {
        var me = this,
            grid = me.grid,
            emptyCmp = me.emptyComponent,
            previewCmp = me.previewComponent;

        if (!(emptyCmp instanceof Ext.Component)) {
            emptyCmp = Ext.clone(emptyCmp);
        }
        me.items[0] = emptyCmp;

        if (!(grid instanceof Ext.Component)) {
            grid = Ext.clone(grid);
        }

        me.items[1].items.push(grid);

        if (!(previewCmp instanceof Ext.Component)) {
            previewCmp = Ext.clone(previewCmp);
        }

        me.items[1].items.push(previewCmp);

        this.callParent(arguments);

        debugger;
        me.grid = me.getWrapperCt().items.items[0];
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
        var me = this;

        me.getLayout().setActiveItem(1);
        me.setVisible(true);
    },

    onLoad: function () {
        var me = this,
            count = me.grid.store.getCount(),
            isEmpty = count === 0;

        me.getLayout().setActiveItem(isEmpty ? 0 : 1);
        me.setVisible(true);
    },

    getWrapperCt: function () {
        return this.down('#wrapper-container');
    }
});