/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.grid.plugin.RowEditing', {
    override: 'Ext.grid.plugin.RowEditing',
    
    initEditorConfig: function(){
        var cfg = this.callParent();
        cfg.rtl = this.grid.getHierarchyState().rtl;
        return cfg;
    }
});
