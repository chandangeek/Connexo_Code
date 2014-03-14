/**
 * This class is used as a mixin.
 *
 * This class is to be used to provide basic methods for controllers that handle events of grid view.
 */
Ext.define('Isu.util.IsuGrid', {
    /**
     * Handle 'itemmouseenter' event.
     * Show tooltip for user type icon.
     */
    onUserTypeIconHover: function (view, record, item) {
        var rowEl = Ext.get(item),
            iconElem = rowEl.select('span').elements[0],
            toolTip;
        if (iconElem) {
            var icon = iconElem.getAttribute('class'),
                domIconElem = Ext.get(iconElem);
            switch (icon) {
                case 'isu-icon-USER':
                    toolTip = Ext.create('Ext.tip.ToolTip', {
                        target: domIconElem,
                        html: 'User',
                        style: {
                            borderColor: 'black'
                        }
                    });
                    break;
                case 'isu-icon-GROUP':
                    toolTip = Ext.create('Ext.tip.ToolTip', {
                        target: domIconElem,
                        html: 'User group',
                        style: {
                            borderColor: 'black'
                        }
                    });
                    break;
                case 'isu-icon-ROLE':
                    toolTip = Ext.create('Ext.tip.ToolTip', {
                        target: domIconElem,
                        html: 'User role',
                        style: {
                            borderColor: 'black'
                        }
                    });
                    break;
                default:
                    break;
            }
            domIconElem.on('mouseenter', function () {
                toolTip.show();
            });
            domIconElem.on('mouseleave', function () {
                toolTip.hide();
            });
        }
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
                record: record
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