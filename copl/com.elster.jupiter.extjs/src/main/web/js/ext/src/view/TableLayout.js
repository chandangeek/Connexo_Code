/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
/**
 *  Component layout for {@link Ext.view.Table}
 *  @private
 * 
 */
Ext.define('Ext.view.TableLayout', {
    extend: 'Ext.layout.component.Auto',
    requires: ['Ext.util.CSS'],

    alias: ['layout.tableview'],
    type: 'tableview',

    beginLayout: function(ownerContext) {
        var me = this,
            otherSide = me.owner.lockingPartner,
            owner = me.owner;

        me.callParent(arguments);
        
        // If we are in a twinned grid (locked view) then set up bidirectional links with the other side's layout context
        if (otherSide) {
            me.lockedGrid = me.owner.up('[lockable]');
            me.lockedGrid.needsRowHeightSync = true;
            if (!ownerContext.lockingPartner) {
                ownerContext.lockingPartner = ownerContext.context.getItem(otherSide, otherSide.el);
                if (ownerContext.lockingPartner && !ownerContext.lockingPartner.lockingPartner) {
                    ownerContext.lockingPartner.lockingPartner = ownerContext;
                }
            }
        }

        // Grab a ContextItem for the header container
        ownerContext.headerContext = ownerContext.context.getCmp(me.headerCt);

        // Grab ContextItem for the table only if there is a table to size
        if (me.owner.body.dom) {
            ownerContext.bodyContext = ownerContext.getEl(me.owner.body);
        }
        if (Ext.isWebKit) {
            owner.el.select(owner.getBodySelector()).setStyle('table-layout', 'auto');
        }
    },

    calculate: function(ownerContext) {
        var me = this,
            lockingPartner = me.lockingPartner,
            owner = me.owner,
            contentHeight = 0,
            emptyEl;

        // We can only complete our work (setting the CSS rules governing column widths) if the
        // Grid's HeaderContainer's ColumnLayout has set the widths of its columns.
        if (ownerContext.headerContext.hasProp('columnWidthsDone')) {
            if (!me.setColumnWidths(ownerContext)) {
                me.done = false;
                return;
            }
            ownerContext.state.columnWidthsSynced = true;
            if (ownerContext.bodyContext) {
                emptyEl = me.owner.el.down('.' + owner.ownerCt.emptyCls, true);
                if (!emptyEl) {
                    contentHeight = ownerContext.bodyContext.el.dom.offsetHeight;
                    ownerContext.bodyContext.setHeight(contentHeight, false);
                } else {
                    contentHeight = emptyEl.offsetHeight;
                }
                // If there is horizontal overflow, and the grid is shrinkwrapping height, then allow the horizontal scrollbar to contibute to contentHeight
                if (ownerContext.headerContext.state.boxPlan.tooNarrow && ownerContext.ownerCtContext.sizeModel.height.shrinkWrap) {
                    contentHeight += Ext.getScrollbarSize().height;
                }
                ownerContext.setProp('contentHeight', contentHeight);
            }

            // If we are part of a twinned table view set (locking grid)
            // Then only complete when both sides are complete.
            if (lockingPartner && !lockingPartner.state.columnWidthsSynced) {
                me.done = false;
            } else {
                me.callParent(arguments);
            }

        } else {
            me.done = false;
        }
    },

    measureContentHeight: function(ownerContext) {
        var lockingPartner = ownerContext.lockingPartner;

        // Only able to produce a valid contentHeight if there's no table
        // ... or we have flushed all column widths to the table (or both tables if we are a pair)
        if (!ownerContext.bodyContext || (ownerContext.state.columnWidthsSynced && (!lockingPartner || lockingPartner.state.columnWidthsSynced))) {
            return this.callParent(arguments);
        }
    },

    setColumnWidths: function(ownerContext) {
        // No content to size. We're done
        if (!this.owner.body.dom) {
            return true;
        }

        var me = this,
            owner = me.owner,
            context = ownerContext.context,
            columns = me.headerCt.getVisibleGridColumns(),
            column,
            i,
            len = columns.length,
            tableWidth = 0,
            columnLineWidth = 0,
            childContext,
            colWidth,
            isContentBox = !Ext.isBorderBox,
            colGroup = owner.body.dom.firstChild,
            isColGroup = colGroup.tagName.toUpperCase() === 'COLGROUP',
            changedColumns = [];

        // So that the setProp can trigger this layout.
        if (context) {
            context.currentLayout = me;
        }

        // Collect columns which have a changed width
        for (i = 0; i < len; i++) {
            column = columns[i];
            childContext = context.getCmp(column);

            // Only bother with columns with changed width since last fluch of ColumnLayout, or since last setColumnWidths call.
            if (!column.lastBox || column.lastBox.invalid || childContext.props.width !== column.lastBox.width || (childContext.cellWidth && childContext.cellWidth != childContext.props.width)) {
                colWidth = childContext.props.width;
                if (isNaN(colWidth)) {
                    // We don't have a width set, so we need to trigger when this child
                    // actually gets a width assigned so we can continue. Technically this
                    // shouldn't happen however we have a bug inside ColumnLayout where
                    // columnWidthsDone is set incorrectly. This is just a workaround.
                    childContext.getProp('width');
                    return false;
                }
                tableWidth += colWidth;
                childContext.columnIndex = i;
                changedColumns.push(childContext);
            } else {
                tableWidth += column.lastBox.width;
            }

            // Track flushed cell width so we can check for changes.
            childContext.cellWidth = childContext.props.width;
        }

        // If no columns need changing, we're done
        len = changedColumns.length;
        if (!len) {
            return true;
        }

        // Set width of main table
        owner.body.setWidth(tableWidth);

        // Set column width corresponding to each header
        for (i = 0; i < len; i++) {
            childContext = changedColumns[i];
            colWidth = childContext.props.width;

            // https://sencha.jira.com/browse/EXTJSIV-9263 - Browsers which cannot be switched to border box when doctype present (IE6 & IE7) - must subtract borders width from width of cells.
            // TODO: Remove this when IE7 & IE7 are dropped.
            if (isContentBox && owner.columnLines) {
                if (!columnLineWidth) {
                    columnLineWidth = ownerContext.headerContext.childItems[0].borderInfo.width;
                }
                colWidth -= columnLineWidth;
            }

            // Resize the <col> elements within the single <colgroup> element which is at the top of a grid's <table>
            // WHEN IT IS NOT USING RowWrap.
            // On IE8, sizing <col> elements to control column width was about 2.25 times
            // faster than selecting all the cells in the column to be resized.
            // Column sizing using dynamic CSS rules is *extremely* expensive on IE.
            if (isColGroup) {
                colGroup.childNodes[childContext.columnIndex].style.width = colWidth + 'px';
            }

            // This will be the slow bit.
            // Sizing changed columns using DomQuery.
            if (owner.features.length) {
                owner.body.select(owner.getColumnSizerSelector(childContext.target)).setWidth(colWidth);
            }
        }

        return true;
    },

    finishedLayout: function() {
        var me = this,
            owner = me.owner;

        me.callParent(arguments);

        if (Ext.isWebKit) {
            owner.el.select(owner.getBodySelector()).setStyle('table-layout', '');
        }
        // Make sure only one side gets to do the sync on completion - it's an expensive process.
        // Only do it if the syncRowHeightConfig hasn't been set to false.
        if (owner.refreshCounter && me.lockedGrid && me.lockedGrid.syncRowHeight && me.lockedGrid.needsRowHeightSync) {
            me.lockedGrid.syncRowHeights();
            me.lockedGrid.needsRowHeightSync = false;
        }
    }
});