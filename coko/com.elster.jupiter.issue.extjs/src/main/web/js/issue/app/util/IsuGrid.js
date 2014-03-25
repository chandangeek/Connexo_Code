/**
 * This class is used as a mixin.
 *
 * This class is to be used to provide basic methods for controllers that handle events of grid view.
 */
Ext.define('Isu.util.IsuGrid', {
    /**
     * Handle 'refresh' event.
     * Set tooltip for assignee type icon.
     * 'class' property of element must be equal 'isu-assignee-type-icon'.
     */
    setAssigneeTypeIconTooltip: function (grid) {
        var gridEl = grid.getEl(),
            icons = gridEl.query('.isu-assignee-type-icon');

        Ext.Array.each(icons, function (item) {
            var icon = Ext.get(item),
                text;

            if (icon.hasCls('isu-icon-USER')) {
                text = 'User';
            } else if (icon.hasCls('isu-icon-GROUP')) {
                text = 'User group';
            } else if (icon.hasCls('isu-icon-ROLE')) {
                text = 'User role';
            }

            if (text) {
                icon.tooltip = Ext.create('Ext.tip.ToolTip', {
                    target: icon,
                    html: text,
                    style: {
                        borderColor: 'black'
                    }
                });

                icon.on('mouseenter', function () {
                    icon.tooltip.show();
                });
                icon.on('mouseleave', function () {
                    icon.tooltip.hide();
                });
                grid.on('destroy', function () {
                    icon.tooltip.destroy();
                });
                grid.on('beforerefresh', function () {
                    icon.tooltip.destroy();
                });
            }
        });
    },

    /**
     * Handle 'refresh' event.
     * Set tooltip for description cell if inner text is shown with ellipsis.
     * 'rtdCls' property of column must be equal 'isu-grid-description'.
     */
    setDescriptionTooltip: function (grid) {
        var gridEl = grid.getEl(),
            descriptionCells = gridEl.query('.isu-grid-description');

        Ext.Array.each(descriptionCells, function (item) {
            var cell = Ext.get(item),
                cellInner = cell.down('.x-grid-cell-inner'),
                text = cellInner.getHTML();

            cell.tooltip = Ext.create('Ext.tip.ToolTip', {
                target: cell,
                html: text,
                style: {
                    borderColor: 'black'
                }
            });

            cell.on('mouseenter', function () {
                cell.tooltip.show();
            });
            cell.on('mouseleave', function () {
                cell.tooltip.hide();
            });
            grid.on('destroy', function () {
                cell.tooltip.destroy();
            });
            grid.on('beforerefresh', function () {
                cell.tooltip.destroy();
            });
        });
    },

    /**
     * Handle actioncolumn 'click' event.
     * Show menu for actioncolumn icon.
     */
    showItemAction: function (grid, cell, rowIndex, colIndex, e, record) {
        var cellEl = Ext.get(cell);

        this.hideItemAction();

        this.gridActionIcon = cellEl.first();

        this.gridActionIcon.hide();
        this.gridActionIcon.setHeight(0);
        this.gridActionBtn = Ext.create('widget.grid-action', {
            renderTo: cell,
            menu: {
                xtype: this.actionMenuXtype,
                issueId: record.data.id
            }
        });
        this.gridActionBtn.showMenu();
    },

    /**
     * Hide menu for actioncolumn icon.
     */
    hideItemAction: function () {
        if (this.gridActionBtn) {
            this.gridActionBtn.destroy();
        }

        if (this.gridActionIcon) {
            this.gridActionIcon.show();
            this.gridActionIcon.setHeight(22);
        }
    },

    /**
     * Handle 'itemclick' event.
     * Load item model and fire event for item panel view.
     */
    loadGridItemDetail: function (grid, record) {
        var itemPanel = this.getItemPanel(),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if (this.displayedItemId != record.id) {
            grid.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.id;
        this.gridItemModel.load(record.data.id, {
            success: function (rec) {
                itemPanel.fireEvent('change', itemPanel, rec);
                preloader.destroy();
            }
        });
    }
});