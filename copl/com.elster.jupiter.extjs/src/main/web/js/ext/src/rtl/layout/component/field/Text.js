/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.layout.component.field.Text', {
    override: 'Ext.layout.component.field.Text',

    adjustIEInputPadding: function(ownerContext) {
        var owner = this.owner;

        // adjust for IE 6/7 strict content-box model
        owner.bodyEl.setStyle(
            owner.getHierarchyState().rtl ? 'padding-left' : 'padding-right',
            this.ieInputWidthAdjustment + 'px'
        );
    }
});