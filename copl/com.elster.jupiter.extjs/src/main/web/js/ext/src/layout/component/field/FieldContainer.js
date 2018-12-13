/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
/**
 * @private
 */
Ext.define('Ext.layout.component.field.FieldContainer', {

    /* Begin Definitions */

    extend: 'Ext.layout.component.field.Field',

    alias: 'layout.fieldcontainer',

    /* End Definitions */

    type: 'fieldcontainer',

    waitForOuterHeightInDom: true,
    waitForOuterWidthInDom: true,

    beginLayout: function(ownerContext) {
        var owner = this.owner;
        this.callParent(arguments);

        // Tell Component.measureAutoDimensions to measure the DOM when containerChildrenSizeDone is true
        ownerContext.hasRawContent = true;
        owner.bodyEl.setStyle('height', '');
        owner.containerEl.setStyle('height', '');
        ownerContext.containerElContext = ownerContext.getEl('containerEl');
    },

    measureContentHeight: function (ownerContext) {
        // since we are measuring the outer el, we have to wait for whatever is in our
        // container to be flushed to the DOM... especially for things like box layouts
        // that size the innerCt since that is all that will contribute to our size!
        return ownerContext.hasDomProp('containerLayoutDone') ? this.callParent(arguments) : NaN;
    },

    measureContentWidth: function (ownerContext) {
        // see measureContentHeight
        return ownerContext.hasDomProp('containerLayoutDone') ? this.callParent(arguments) : NaN;
    },

    publishInnerWidth: function (ownerContext, width) {
        var bodyContext = ownerContext.bodyCellContext,
            innerWidth = bodyContext.el.getWidth() - bodyContext.getPaddingInfo().width;

        bodyContext.setWidth(innerWidth, false);
        ownerContext.containerElContext.setWidth(innerWidth, false);
    },
    
    publishInnerHeight: function (ownerContext, height) {
        var bodyContext = ownerContext.bodyCellContext,
            containerElContext = ownerContext.containerElContext;
            
        height -= this.measureLabelErrorHeight(ownerContext) + bodyContext.getPaddingInfo().height;
        bodyContext.setHeight(height);
        containerElContext.setHeight(height);
    }
});
