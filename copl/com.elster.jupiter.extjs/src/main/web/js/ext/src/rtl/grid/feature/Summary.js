/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.grid.feature.Summary', {
    override: 'Ext.grid.feature.Summary',
    
    init: function(){
        this.callParent(arguments);
        if (this.view.getHierarchyState().rtl) {
            this.scrollPadProperty = 'padding-left';
        }
    }
});