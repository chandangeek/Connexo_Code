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
                icon.set({'data-qtip': text});
            }
        });
    },

    /**
     * Handle 'select' event.
     * Load item model and fire event for item panel view.
     */
    loadGridItemDetail: function (selectionModel, record) {
        var itemPanel = this.getItemPanel(),
            form = itemPanel.down('form');

        itemPanel.setLoading(true);

        this.gridItemModel.load(record.getId(), {
            success: function (record) {
                if (!form.isDestroyed) {
                    form.loadRecord(record);
                    form.up('panel').down('menu').record = record;
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