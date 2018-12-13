/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.resizer.BorderSplitterTracker', {
    override: 'Ext.resizer.BorderSplitterTracker',

    rtlDirections: {
        top: 'top',
        right: 'left',
        bottom: 'bottom',
        left: 'right'
    },

    getCollapseDirection: function() {
        var direction = this.splitter.getCollapseDirection();
        if (!this.splitter.getHierarchyState().rtl !== !Ext.rootHierarchyState.rtl) {
            direction = this.rtlDirections[direction];
        }
        return direction;
    }
});