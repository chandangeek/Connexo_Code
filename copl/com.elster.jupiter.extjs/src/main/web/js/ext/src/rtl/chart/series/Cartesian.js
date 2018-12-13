/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.chart.series.Cartesian', {
    override: 'Ext.chart.series.Cartesian',
    
    initialize: function() {
        var me = this;
        
        me.callParent(arguments);
        me.axis = me.chart.invertPosition(me.axis); 
        if (me.chart.getHierarchyState().rtl) {
            me.reverse = true;
        }
    }
});
