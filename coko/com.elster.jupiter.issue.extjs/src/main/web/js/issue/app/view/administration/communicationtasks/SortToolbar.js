Ext.define('Isu.view.administration.communicationtasks.SortToolbar', {
    extend: 'Skyline.panel.FilterToolbar',
    requires: [
        'Skyline.button.SortItemButton',
        'Isu.view.administration.communicationtasks.SortMenu'
    ],
    alias: 'widget.communication-tasks-sort-toolbar',
    title: 'Sort',
    name: 'sortitemspanel',
    emptyText: 'None',
    tools: [
        {
            xtype: 'button',
            action: 'addSort',
            text: 'Add sort',
            menu: {
                xtype: 'communication-tasks-sort-menu'
            }
        }
    ],

    addSortButtons: function (sortModel) {
        var self = this,
            container = self.getContainer(),
            data = sortModel.getData(),
            menuItem,
            cls;

        Ext.Object.each(data, function (key, value) {
            if (key != 'id' && value) {
                menuItem = self.down('communication-tasks-sort-menu [action=' + key + ']');
                cls = value == Isu.model.IssueSort.ASC
                    ? 'x-btn-sort-item-asc'
                    : 'x-btn-sort-item-desc';

                container.add({
                    xtype: 'sort-item-btn',
                    text: menuItem.text,
                    sortName: key,
                    sortDirection: value,
                    iconCls: cls
                });
            }
        });
    }
});