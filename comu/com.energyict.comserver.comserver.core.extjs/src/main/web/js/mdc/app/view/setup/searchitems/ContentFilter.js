Ext.define('Mdc.view.setup.searchitems.ContentFilter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Ext.form.Label',
        'Mdc.view.setup.searchitems.SortMenu',
        'Mdc.model.ItemSort'
    ],
    alias: "widget.search-content-filter",
    id: 'search-content-filter-id',
    border: true,
    header: false,
    collapsible: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },


    items: [
        {
            title: 'Filters',
            xtype: 'filter-toolbar',
            name: 'filter',
            emptyText: 'None'
        },
        { xtype: 'menuseparator' },

// Sort
        {
            xtype: 'filter-toolbar',
            title: 'Sort',
            name: 'sortitemspanel',
            itemId: 'sortitemid',
            emptyText: 'None',
            tools: [
                {
                    xtype: 'button',
                    action: 'addSort',
                    text: 'Add sort',
                    menu: {
                        xtype: 'items-sort-menu',
                        name: 'addsortitemmenu'
                    }
                }
            ]
        }
    ],

    addSortButtons: function () {
        debugger;
        var self = this.down('#sortitemid'),
            container = self.getContainer(),
            data = new Mdc.model.ItemSort().getData(),
            menuItem,
            cls;

        container.removeAll();
        Ext.Object.each(data, function (key, value) {
            if (key != 'id' && value) {
                menuItem = self.down('items-sort-menu [action=' + key + ']');
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