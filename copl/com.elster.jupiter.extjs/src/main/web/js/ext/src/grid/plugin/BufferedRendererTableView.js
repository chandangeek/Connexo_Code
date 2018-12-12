/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
/**
 * @private
 * A set of overrides required by the presence of the BufferedRenderer plugin.
 * 
 * These overrides of Ext.view.Table take into account the affect of a buffered renderer and
 * divert execution from the default course where necessary.
 */
Ext.define('Ext.grid.plugin.BufferedRendererTableView', {
    override: 'Ext.view.Table',

    onReplace: function(store, startIndex, oldRecords, newRecords) {
        var me = this,
            bufferedRenderer = me.bufferedRenderer;

        // If there's a buffered renderer and the removal range falls inside the current view...
        if (me.rendered && bufferedRenderer) {
            bufferedRenderer.onReplace(store, startIndex, oldRecords, newRecords);
        } else {
            me.callParent(arguments);
        }
    },

    // Listener function for the Store's add event
    onAdd: function(store, records, index) {
        var me = this,
            bufferedRenderer = me.bufferedRenderer;

        if (me.rendered && bufferedRenderer) {
             bufferedRenderer.onReplace(store, index, [], records);
        }
        // No BufferedRenderer present
        else {
            me.callParent([store, records, index]);
        }
    },

    onRemove: function(store, records, indices, isMove, removeRange) {
        var me = this,
            bufferedRenderer = me.bufferedRenderer;

        // If there's a BufferedRenderer...
        if (me.rendered && bufferedRenderer) {

            // If it's a contiguous range, the replace processing can handle it.
            if (removeRange) {
                bufferedRenderer.onReplace(store, indices[0], records, []);
            }

            // Otherwise it's a refresh
            else {
                bufferedRenderer.refreshView();
            }
        } else {
            me.callParent([store, records, indices]);
        }
    },

    // When there's a buffered renderer present, store refresh events cause TableViews to go to scrollTop:0
    onDataRefresh: function() {
        var me = this;

        if (me.bufferedRenderer) {
            // Clear NodeCache. Do NOT remove nodes from DOM - that would blur the view, and then refresh will not refocus after the refresh.
            me.all.clear();
            me.bufferedRenderer.onStoreClear();
        }
        me.callParent();
    }
});
