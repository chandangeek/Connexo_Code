Ext.define('Usr.view.group.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.groupList',
    itemId: 'groupList',
    store: 'Usr.store.Groups',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    columns: {
        defaults: {
            flex: 1,
            sortable: false,
            hideable: false,
            fixed: true
        },
        items: [
            {
                header: Uni.I18n.translate('group.name', 'USM', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('group.description', 'USM', 'Description'),
                dataIndex: 'description',
                flex: 7
            },
            {
                xtype:'actioncolumn',
                tdCls:'view',
                header : Uni.I18n.translate('general.actions', 'USM', 'Actions'),
                flex: 0.5,
                items: [{
                    iconCls: 'x-uni-action-icon',
                    handler: function(grid, rowIndex, colIndex,item,e) {
                        var menu = Ext.widget('menu', {
                            itemId: 'menuGroupsList',
                            items: [{
                                xtype: 'menuitem',
                                itemId: 'menuGroupsListEdit',
                                text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
                                listeners: {
                                    click: {
                                        element: 'el',
                                        fn: function(){
                                            this.fireEvent('editGroupItem',grid.getRecord(rowIndex));
                                        },
                                        scope: this
                                    }

                                }
                            }]
                        });

                        menu.showAt(e.getXY());
                    }
                }]
            }
        ]
    },

    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            store: this.store,
            dock: 'top',
            displayMsg: Uni.I18n.translate('group.list.top', 'USM', '{0} - {1} of {2} roles'),
            items: [
                '->',
                {
                    text: Uni.I18n.translate('group.create', 'USM', 'Create role'),
                    action: 'createGroup'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            store: this.store,
            dock: 'bottom',
            Limit: '10',
            itemsPerPageMsg: Uni.I18n.translate('group.list.bottom', 'USM', 'Roles per page')
        }
    ]
});