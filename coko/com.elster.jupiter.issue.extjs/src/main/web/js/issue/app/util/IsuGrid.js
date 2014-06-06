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
                    html: text
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
                html: text
            });

            grid.on('destroy', function () {
                cell.tooltip && cell.tooltip.destroy();
            });
            grid.on('beforerefresh', function () {
                cell.tooltip && cell.tooltip.destroy();
            });
        });
    },

    /**
     * Handle 'itemclick' event.
     * Load item model and fire event for item panel view.
     */
    loadGridItemDetail: function (grid, record) {
        var itemPanel = this.getItemPanel(),
            form = itemPanel.down('form');

        if (this.displayedItemId != record.id) {
            grid.clearHighlight();
            itemPanel.setLoading(true);
        }
        this.displayedItemId = record.id;
        this.gridItemModel.load(record.data.id, {
            success: function (record) {
                if (!form.isDestroyed) {
                    form.loadRecord(record);
                    form.up('panel').down('item-action').menu.record = record;
                    itemPanel.setLoading(false);
                    itemPanel.fireEvent('afterChange',itemPanel);
                    itemPanel.setTitle(record.data.title);
                }
            }
        });
    },

    /**
     * Handle 'refresh' event.
     * Select first row in grid.
     */

    selectFirstGridRow: function (grid) {
        var itemPanel = this.getItemPanel(),
            index = 0,
            item = grid.getNode(index),
            record;

        if (item) {
            itemPanel.show();
            record = grid.getRecord(item);
            grid.fireEvent('itemclick', grid, record, item, index);
        } else {
            itemPanel.hide();
        }
    }
});