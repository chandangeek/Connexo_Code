/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.selection.CellModel', {
    override: 'Ext.selection.CellModel',
    
    doMove: function(direction, e) {
        if (this.view.getHierarchyState().rtl) {
            if (direction == 'left') {
                direction = 'right';
            } else if (direction == 'right') {
                direction = 'left';
            }
        }
        this.callParent([direction, e]);
    }
})
