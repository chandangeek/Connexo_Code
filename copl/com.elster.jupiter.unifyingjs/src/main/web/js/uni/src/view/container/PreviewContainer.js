/**
 * @class Uni.view.container.PreviewContainer
 *
 * The {@link Uni.view.container.EmptyGridContainer} shows a custom component when the grid
 * it is displaying does not have any data. By default it shows the grid, after the store is
 * done loading with no items then the custom {@link #emptyComponent} will be shown.
 *
 * The {@link #grid} needs to be of the type {@link Ext.grid.Panel} and have a valid store
 * attached as property. While the {@link #emptyComponent} and {@link #previewComponent}
 * can be any type of component.
 *
 * # How to use
 *
 *     @example
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
    itemId: 'preview-container',

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

    /**
     * @cfg {Object/Ext.Component}
     *
     * Component to show the preview in of the selected row. This is hidden when there are no rows.
     */
    previewComponent: null,

    /**
     * @cfg {Boolean}
     *
     * Select a row by default or not, by default true.
     */
    selectByDefault: true,

    /**
     * @cfg {Boolean}
     *
     * Hide empty compoonent if set to true.
     */
    hasNotEmptyComponent: false,
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
            items: []
        }
    ],

    initComponent: function () {
        var me = this,
            grid = me.grid,
            emptyCmp = me.emptyComponent,
            previewCmp = me.previewComponent;

        // Empty component.

        if (!(emptyCmp instanceof Ext.Component)) {
            emptyCmp = Ext.clone(emptyCmp);
        }
        me.items[0] = emptyCmp;

        // Grid and preview component.

        me.items[1].items = [];

        if (!(grid instanceof Ext.Component)) {
            grid = Ext.clone(grid);
        }

        // TODO Hardcoded height until [JP-2852] is implemented.
        grid.maxHeight = 450;

        me.items[1].items.push(grid);

        if (!(previewCmp instanceof Ext.Component)) {
            previewCmp = Ext.clone(previewCmp);
        }

        me.items[1].items.push(previewCmp);

        // Continue.

        me.callParent(arguments);

        me.grid = me.getWrapperCt().items.items[0];
        me.previewComponent = me.getWrapperCt().items.items[1];

        me.bindStore(me.grid.store || 'ext-empty-store', true);
        me.initChildPagingBottom();
        me.initGridListeners();

        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    doChildPagingOperation: function (xtype, operation) {
        var me = this,
            pagingComponent;

        if (Ext.isDefined(me.previewComponent)) {
            pagingComponent = me.previewComponent.down(xtype);
        } else {
            return;
        }

        if (pagingComponent !== null
            && Ext.isDefined(pagingComponent)
            && pagingComponent.getXType() === xtype) {
            pagingComponent.updatePagingParams = false;
            operation(pagingComponent);
        }
    },

    resetChildPagingTop: function () {
        var me = this,
            pagingTopXType = 'pagingtoolbartop';

        me.doChildPagingOperation(pagingTopXType, function (pagingComponent) {
            pagingComponent.resetPaging();
        });
    },

    initChildPagingBottom: function () {
        var me = this,
            pagingBottomXType = 'pagingtoolbarbottom';

        me.doChildPagingOperation(pagingBottomXType, function (pagingComponent) {
            pagingComponent.updatePagingParams = false;
        });
    },

    resetChildPagingBottom: function () {
        var me = this,
            pagingBottomXType = 'pagingtoolbarbottom';

        me.doChildPagingOperation(pagingBottomXType, function (pagingComponent) {
            pagingComponent.resetPaging();
        });
    },

    initGridListeners: function () {
        var me = this;

        me.grid.on('selectionchange', me.onGridSelectionChange, me);
    },

    onGridSelectionChange: function () {
        var me = this,
            selection = me.grid.view.getSelectionModel().getSelection();

        if (me.rendered) {
            Ext.suspendLayouts();
        }

        if (me.previewComponent) {
            me.previewComponent.setVisible(selection.length === 1);
        }

        me.resetChildPagingTop();
        me.resetChildPagingBottom();

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            bulkremove: this.onChange,
            remove: this.onChange,
            clear: this.onChange,
            load: this.onLoad
        };
    },

    onBeforeDestroy: function () {
        this.bindStore('ext-empty-store');
    },

    onBeforeLoad: function () {
        var me = this,
            activeIndex = me.items.indexOf(me.getLayout().getActiveItem());

        try {
            me.grid.getView().getSelectionModel().deselectAll(true);
        } catch (ex) {
            // Ignore exceptions for when the selection model is not ready yet.
        }

        if (me.hasNotEmptyComponent) {
            me.hide();
            return;
        }

        if (activeIndex !== 1) {
            me.getLayout().setActiveItem(1);
        }
    },

    onLoad: function (store, records) {
        this.updateOnChange(records.length === 0);
    },

    onChange: function(store) {
        this.updateOnChange(store.getCount() === 0);
    },

    updateOnChange: function(isEmpty) {
        var me = this,
            activeIndex = me.items.indexOf(me.getLayout().getActiveItem());
        if (isEmpty && activeIndex !== 0) {
            me.getLayout().setActiveItem(0);
        } else if (!isEmpty && activeIndex !== 1) {
            if (me.hasNotEmptyComponent) {
                me.show();
                me.getLayout().setActiveItem(0);
            } else {
                me.getLayout().setActiveItem(1);
            }
        }

        if (me.selectByDefault && !isEmpty) {
            me.grid.getView().getSelectionModel().preventFocus = true;
            me.grid.getView().getSelectionModel().select(0);
        }
    },

    getWrapperCt: function () {
        return this.down('#wrapper-container');
    }
});