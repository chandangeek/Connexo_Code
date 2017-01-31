/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.grid.RowEditor', {
    override: 'Ext.grid.RowEditor',

    setButtonPosition: function(btnEl, left){
        if (this.getHierarchyState().rtl) {
            btnEl.rtlSetLocalXY(left, this.el.dom.offsetHeight - 1);
        } else {
            this.callParent(arguments);
        }
    }
});
