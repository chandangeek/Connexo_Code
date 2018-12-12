/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.layout.container.Column', {
    override: 'Ext.layout.container.Column',

    // Override to put the RTL class onto the innerCt so that columns can have a rule which switches float direction
    getRenderData: function () {
        var renderData = this.callParent();

        if (this.owner.getHierarchyState().rtl) {
            renderData.innerCtCls =
                (renderData.innerCtCls || '') + ' ' + Ext.baseCSSPrefix + 'rtl';
        }
        
        return renderData;
    }
});
