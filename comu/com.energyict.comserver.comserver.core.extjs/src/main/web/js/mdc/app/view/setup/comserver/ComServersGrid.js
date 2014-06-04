Ext.define('Mdc.view.setup.comserver.ComServersGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comServersGrid',
    store: 'ComServers',
    overflowY: 'auto',
    itemId: 'comservergrid',
    padding: 10,
    selModel: {
        mode: 'MULTI'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    selType: 'checkboxmodel',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.comserverType', 'MDC', 'Comserver type'),
                dataIndex: 'comServerType',
                flex: 1
            },
            {
                dataIndex: 'active',
                width: 24,
                renderer: function (value, metadata) {
                    if (value === true) {
                        metadata.style = "background-color:lightgreen;";
                    } else {
                        metadata.style = "background-color:pink;";
                    }
                }
            },
            {
                xtype: 'actioncolumn',
                iconCls: 'uni-actioncolumn-gear',
                columnWidth: 32,
                fixed: true,
                header: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                sortable: false,
                hideable: false,
                items: [
                    {
                        handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                            var menu = Ext.widget('menu', {
                                items: [
                                    {
                                        xtype: 'menuitem',
                                        text: 'Edit',
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('edit', record);
                                                },
                                                scope: this
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'menuitem',
                                        text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    console.log('deleteItem');
                                                    this.fireEvent('deleteItem', record);
                                                },
                                                scope: this
                                            }
                                        }
                                    }
                                ]
                            });
                            menu.showAt(e.getXY());
                        }
                    }
                ]
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom'
            },
            {
                xtype: 'toolbar',
                dock: 'top',
                ui: 'footer',
                defaults: {minWidth: this.minButtonWidth},
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: 'Add',
                        action: 'add',
                        menu: [
                            {
                                text: 'Online'
                            },
                            {
                                text: 'Remote'
                            },
                            {
                                text: 'Mobile'
                            }
                        ]
                    },
                    {
                        text: 'Delete',
                        action: 'delete'
                    }
                ]
            }
        ];

        this.callParent();
    }
});