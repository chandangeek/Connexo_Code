Ext.define('Usr.view.group.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.groupList',
    itemId: 'groupList',
    store: 'Groups',

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
                header: Uni.I18n.translate('group.name', 'USM', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                flex: 3
            },
            {
                header: Uni.I18n.translate('group.description', 'USM', 'Description'),
                dataIndex: 'description',
                sortable: false,
                hideable: false,
                flex: 7
            },
            {
                xtype:'actioncolumn',
                tdCls:'view',
                header : Uni.I18n.translate('general.actions', 'USM', 'Actions'),
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
                                            this.fireEvent('editGroupItem',grid.getSelectionModel().getSelection()[0]);
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
                displayMsg: Uni.I18n.translate('group.list.top', 'USM', '{0} - {1} of {2} roles'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        //xtype: 'button',
                        text: Uni.I18n.translate('group.create', 'USM', 'Create role'),
                        action: 'createGroup'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('group.list.bottom', 'USM', 'Roles per page')
            }
        ];
        this.callParent(arguments);
    }
});