/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.panel.Panel', {
    override: 'Ext.panel.Panel',

    rtlCollapseDirs: {
        top: 'top',
        right: 'left',
        bottom: 'bottom',
        left: 'right'
    },

    convertCollapseDir: function(collapseDir) {
        if (!!Ext.rootHierarchyState.rtl !== this.isLocalRtl()) {
            collapseDir = this.rtlCollapseDirs[collapseDir];
        }
        return this.callParent(arguments);
    }
});
