/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.layout.container.Border', {
    override: 'Ext.layout.container.Border',
    
    initLayout: function(){
        var me = this;
        
        if (me.owner.getHierarchyState().rtl) {
            me.padOnContainerProp = 'right';
            me.padNotOnContainerProp = 'left';  
            me.horzPositionProp = 'right';
        }
        me.callParent(arguments);    
    }
});
