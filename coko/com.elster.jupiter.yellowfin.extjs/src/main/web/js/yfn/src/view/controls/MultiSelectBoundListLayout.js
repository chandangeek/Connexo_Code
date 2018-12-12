/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by Lucian on 12/15/2014.
 */

Ext.define('Yfn.view.controls.MultiSelectBoundListLayout', {
    extend: 'Ext.layout.component.Auto',
    alias: 'layout.multiselectboundlist',

    type: 'component',

    beginLayout: function(ownerContext) {
        var me = this,
            owner = me.owner,
            pagingToolbar = owner.pagingToolbar,
            selectToolbar = owner.selectToolbar;

        me.callParent(arguments);

        if (owner.floating) {
            ownerContext.savedXY = owner.getXY();
            // move way offscreen to prevent any constraining
            // only move on the y axis to avoid triggering a horizontal scrollbar in rtl mode
            owner.setXY([0, -9999]);
        }

        if (pagingToolbar) {
            ownerContext.pagingToolbarContext = ownerContext.context.getCmp(pagingToolbar);
        }
        if (selectToolbar) {
            ownerContext.selectToolbarContext = ownerContext.context.getCmp(selectToolbar);
        }
        ownerContext.listContext = ownerContext.getEl('listEl');
    },

    beginLayoutCycle: function(ownerContext){
        var owner = this.owner;

        this.callParent(arguments);
        if (ownerContext.heightModel.auto) {
            // Set the el/listEl to be autoHeight since they may have been previously sized
            // by another layout process. If the el was at maxHeight first, the listEl will
            // always size to the maxHeight regardless of the content.
            owner.el.setHeight('auto');
            owner.listEl.setHeight('auto');
        }
    },

    getLayoutItems: function() {
        var items = [];
        if(this.owner.pagingToolbar)
            items.push(this.owner.pagingToolbar);
        if(this.owner.selectToolbar)
            items.push(this.owner.selectToolbar);

        return items;
    },

    isValidParent: function() {
        // this only ever gets called with the toolbar, since it's rendered inside we
        // know the parent is always valid
        return true;
    },

    finishedLayout: function(ownerContext) {
        var xy = ownerContext.savedXY;

        this.callParent(arguments);
        if (xy) {
            this.owner.setXY(xy);
        }
    },

    measureContentWidth: function(ownerContext) {
        return this.owner.listEl.getWidth();
    },

    measureContentHeight: function(ownerContext) {
        return this.owner.listEl.getHeight();
    },

    publishInnerHeight: function(ownerContext, height) {
        var pagingToolbar = ownerContext.pagingToolbarContext,
            selectToolbar = ownerContext.selectToolbarContext,
            pagingToolbarHeight = 0,
            selectToolbarHeight = 0
            ;

        if (selectToolbar) {
            selectToolbarHeight = selectToolbar.getProp('height');
        }
        if (pagingToolbar) {
            pagingToolbarHeight = pagingToolbar.getProp('height');
        }

        if (pagingToolbarHeight === undefined || selectToolbarHeight === undefined) {
            this.done = false;
        } else {
            pagingToolbarHeight = pagingToolbarHeight  || 0;
            selectToolbarHeight = selectToolbarHeight  || 0;
            ownerContext.listContext.setHeight(height - ownerContext.getFrameInfo().height - (selectToolbarHeight+pagingToolbarHeight));
        }
    },

    calculateOwnerHeightFromContentHeight: function(ownerContext){
        var height = this.callParent(arguments),
            pagingToolbar = ownerContext.pagingToolbarContext,
            selectToolbar = ownerContext.selectToolbarContext;

        if (pagingToolbar) {
            height += pagingToolbar.getProp('height');
        }
        if (selectToolbar) {
            height += selectToolbar.getProp('height');
        }
        return height;
    }
});