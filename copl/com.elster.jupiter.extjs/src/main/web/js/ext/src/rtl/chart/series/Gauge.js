/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.chart.series.Gauge', {
    override: 'Ext.chart.series.Gauge',
    
    initialize: function() {
        var me = this;
        
        me.callParent(arguments);
        if (me.chart.getHierarchyState().rtl) {
            me.reverse = true;
        }
    }
});
