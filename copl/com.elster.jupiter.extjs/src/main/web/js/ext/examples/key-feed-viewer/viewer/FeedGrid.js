/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('FeedViewer.FeedGrid', {
    extend: 'Ext.grid.Panel',

    alias: 'widget.feedgrid',

    initComponent: function(){
        this.addEvents(
            /**
             * @event rowdblclick
             * Fires when a row is double clicked
             * @param {FeedViewer.FeedGrid} this
             * @param {Ext.data.Model} model
             */
            'rowdblclick',
            /**
             * @event select
             * Fires when a grid row is selected
             * @param {FeedViewer.FeedGrid} this
             * @param {Ext.data.Model} model
             */
            'select'
        );

        Ext.apply(this, {
            cls: 'feed-grid',
            store: Ext.create('Ext.data.Store', {
                model: 'FeedItem',
                sortInfo: {
                    property: 'pubDate',
                    direction: 'DESC'
                },
                proxy: {
                    type: 'ajax',
                    url: 'feed-proxy.php',
                    reader: {
                        type: 'xml',
                        record: 'item'
                    }
                },
                listeners: {
                    load: this.onLoad,
                    scope: this
                }
            }),

            columns: [{
                text: 'Title',
                dataIndex: 'title',
                flex: 1,
                renderer: this.formatTitle
            }, {
                text: 'Author',
                dataIndex: 'author',
                hidden: true,
                width: 200

            }, {
                text: 'Date',
                dataIndex: 'pubDate',
                renderer: this.formatDate,
                width: 200
            }],

            viewConfig: {
                itemId: 'view',
                plugins: [{
                    pluginId: 'preview',
                    ptype: 'preview',
                    bodyField: 'description',
                    expanded: true
                }],
                listeners: {
                    scope: this,
                    dblclick: this.onRowDblClick,
                    afterrender: this.onViewRender
                }
            }
        });
        this.callParent(arguments);
        this.on('selectionchange', this.onSelect, this);
    },

    // private
    onViewRender: function() {
        var me = this;
        me.keyNav = Ext.create('Ext.util.KeyNav', me.items.getAt(0).el, {
            enter: me.onEnterKey,
            scope: me
        });
    },

    onDestroy: function() {
        var me = this;
        Ext.destroy(me.keyNav);
        delete me.keyNav;
        me.callParent(arguments);
    },

    onEnterKey: function(e) {
        var me = this,
            view = me.view,
            idx = view.indexOf(view.getSelectedNodes()[0]);

        me.onRowDblClick(view, idx);
    },

    /**
     * Reacts to a double click
     * @private
     * @param {Object} view The view
     * @param {Object} index The row index
     */
    onRowDblClick: function(view, index){
        this.fireEvent('rowdblclick', this, this.store.getAt(index));
    },


    /**
     * React to a grid item being selected
     * @private
     * @param {Ext.model.Selection} model The selection model
     * @param {Array} selections An array of selections
     */
    onSelect: function(model, selections){
        var selected = selections[0];
        if (selected) {
            this.fireEvent('select', this, selected);
        }
    },

    /**
     * Listens for the store loading
     * @private
     */
    onLoad: function(){
        if (this.getStore().getCount()) {
            this.getSelectionModel().select(0);
        }
    },

    /**
     * Instructs the grid to load a new feed
     * @param {String} url The url to load
     */
    loadFeed: function(url){
        var store = this.store;
        store.getProxy().extraParams.feed = url;
        store.load();
    },

    /**
     * Title renderer
     * @private
     */
    formatTitle: function(value, p, record){
        return Ext.String.format('<div class="topic"><b>{0}</b><span class="author">{1}</span></div>', value, record.get('author'));
    },

    /**
     * Date renderer
     * @private
     */
    formatDate: function(date){
        if (!date) {
            return '';
        }

        var now = new Date(), d = Ext.Date.clearTime(now, true), notime = Ext.Date.clearTime(date, true).getTime();

        if (notime === d.getTime()) {
            return 'Today ' + Ext.Date.format(date, 'g:i a');
        }

        d = Ext.Date.add(d, 'd', -6);
        if (d.getTime() <= notime) {
            return Ext.Date.format(date, 'D g:i a');
        }
        return Ext.Date.format(date, 'Y/m/d g:i a');
    }
});
