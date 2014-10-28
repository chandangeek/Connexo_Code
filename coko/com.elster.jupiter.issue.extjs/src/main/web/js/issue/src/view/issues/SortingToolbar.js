Ext.define('Isu.view.issues.SortingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.SortItemButton',
        'Isu.view.issues.SortingMenu'
    ],
    alias: 'widget.issues-sorting-toolbar',
    title: Uni.I18n.translate('general.sort', 'ISU', 'Sort'),
    emptyText: Uni.I18n.translate('general.none', 'ISU', 'None'),
    showClearButton: false,
    tools: [
        {
            itemId: 'addSort',
            xtype: 'button',
            action: 'addSort',
            text: 'Add sort',
            menu: {
                xtype: 'issues-sorting-menu',
                itemId: 'issues-sorting-menu'
            }
        }
    ],
    addSortButtons: function (sorting) {
        var me = this,
            container = me.getContainer(),
            menuItem,
            cls;

        container.removeAll();
        if (Ext.isArray(sorting)) {
            Ext.Array.each(sorting, function (sortItem) {
                if (sortItem.value) {
                    menuItem = me.down('#issues-sorting-menu [action=' + sortItem.type + ']');
                    cls = sortItem.value === Uni.component.sort.model.Sort.ASC
                        ? 'x-btn-sort-item-asc'
                        : 'x-btn-sort-item-desc';

                    menuItem.hide();
                    container.add({
                        xtype: 'sort-item-btn',
                        itemId: 'issues-sort-by-' + sortItem.type + '-button',
                        text: menuItem.text,
                        sortType: sortItem.type,
                        sortDirection: sortItem.value,
                        iconCls: cls,
                        listeners: {
                            closeclick: function () {
                                me.fireEvent('removeSort', this.sortType, this.sortDirection);
                            },
                            click: function () {
                                me.fireEvent('changeSortDirection', this.sortType, this.sortDirection);
                            }
                        }
                    });
                }
            });
        }
    }
});