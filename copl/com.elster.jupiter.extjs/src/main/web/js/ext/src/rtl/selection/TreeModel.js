/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.selection.TreeModel', {
    override: 'Ext.selection.TreeModel',
    
    onKeyRight: function(e, t) {
        if (this.view.getHierarchyState().rtl) {
            this.navCollapse(e, t);
        } else {
            this.callParent(arguments);
        }
    },

    onKeyLeft: function(e, t) {
        if (this.view.getHierarchyState().rtl) {
            this.navExpand(e, t);
        } else {
            this.callParent(arguments);
        }
    }
});