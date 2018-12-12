/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.layout.component.field.Trigger', {
    override: 'Ext.layout.component.field.Trigger',

    adjustIEInputPadding: function(ownerContext) {
        var owner = this.owner;

        // adjust for IE 6/7 strict content-box model
        owner.inputCell.setStyle(
            owner.getHierarchyState().rtl ? 'padding-left' : 'padding-right',
            this.ieInputWidthAdjustment + 'px'
        );
    }
});