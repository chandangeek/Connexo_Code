/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.chart.axis.Axis', {
    override: 'Ext.chart.axis.Axis',
    
    constructor: function() {
        var me = this,
            pos;
        
        me.callParent(arguments);
        pos = me.position;
        if (me.chart.getHierarchyState().rtl && (pos == 'top' || pos == 'bottom')) {
            me.reverse = true;
        }
    }
})
