/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
/**
 * @private
 * A set of overrides required by the presence of the BufferedRenderer plugin.
 * 
 * These overrides of Ext.tree.View take into account the affect of a buffered renderer and
 * divert execution from the default course where necessary.
 */
Ext.define('Ext.grid.plugin.BufferedRendererTreeView', {
    override: 'Ext.tree.View',

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
    }
});
