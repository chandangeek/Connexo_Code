/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.chart.axis.Gauge', {
    override: 'Ext.chart.axis.Gauge',
    
    constructor: function() {
        var me = this;
        
        me.callParent(arguments);
        if (me.chart.getHierarchyState().rtl) {
            me.reverse = true;
        }
    }
})
