/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.layout.container.Absolute', {
    override: 'Ext.layout.container.Absolute',
    
    adjustWidthAnchor: function(value, childContext) {
        if (this.owner.getHierarchyState().rtl) {
            var padding = this.targetPadding,
                x = childContext.getStyle('right');

            return value - x + padding.right;
        } else {
            return this.callParent(arguments);
        }
    }
})