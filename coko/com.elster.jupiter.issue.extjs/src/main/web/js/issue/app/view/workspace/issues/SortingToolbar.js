Ext.define('Isu.view.workspace.issues.SortingToolbar', {
    extend: 'Skyline.panel.FilterToolbar',
    requires: [
        'Skyline.button.SortItemButton',
        'Isu.view.workspace.issues.SortMenu'
    ],
    alias: 'widget.sorting-toolbar',
    title: 'Sort',
    name: 'sortitemspanel',
    emptyText: 'None',
    tools: [
        {
            xtype: 'button',
            action: 'addSort',
            text: 'Add sort',
            menu: {
                xtype: 'issue-sort-menu'
            }
        }
    ],
    addSortButtons: function (sortModel) {
        var self = this,
            container = self.getContainer(),
            data = sortModel.getData(),
            menuItem,
            cls;

        container.removeAll();
        Ext.Object.each(data, function (key, value) {
            if (key != 'id' && value) {
                menuItem = self.down('issue-sort-menu [action=' + key + ']');
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