Ext.define('Usr.view.user.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.userList',
    itemId: 'userList',
    store: 'Users',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    columns: {
        defaults: {
            flex: 1
        },
        items: [
            {
                header: Uni.I18n.translate('user.name', 'USM', 'Name'),
                dataIndex: 'authenticationName',
                sortable: false,
                hideable: false,
                flex: 3
            },
            {
                header: Uni.I18n.translate('user.description', 'USM', 'Description'),
                dataIndex: 'description',
                sortable: false,
                hideable: false,
                flex: 5
            },
            {
                header: Uni.I18n.translate('user.domain', 'USM', 'Domain'),
                dataIndex: 'domain',
                sortable: false,
                hideable: false,
                flex: 2
            },
            {
                xtype:'actioncolumn',
                tdCls:'view',
                //header : Uni.I18n.translate('general.actions', 'USM', 'Actions'),
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 0.1,
                items: [{
                    icon: '../usr/resources/images/gear-16x16.png',
                    handler: function(grid, rowIndex, colIndex,item,e) {
                        var menu = Ext.widget('menu', {
                                items: [{
                                    xtype: 'menuitem',
                                    text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
                                    listeners: {
                                        click: {
                                            element: 'el',
                                            fn: function(){
                                                this.fireEvent('editUserItem',grid.getSelectionModel().getSelection()[0]);
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

    initComponent: function () {
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('user.list.top', 'USM', '{0} - {1} of {2} users')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('user.list.bottom', 'USM', 'Users per page')
            }
        ];

        this.callParent(arguments);
    }
});