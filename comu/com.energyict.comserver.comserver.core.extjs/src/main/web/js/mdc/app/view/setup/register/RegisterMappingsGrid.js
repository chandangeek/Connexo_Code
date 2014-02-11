Ext.define('Mdc.view.setup.register.RegisterMappingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerMappingsGrid',
    overflowY: 'auto',
    itemId: 'registermappinggrid',
    selModel: {
        mode: 'MULTI'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterMappings'
    ],
    store: 'RegisterMappings',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: 'Name',
                dataIndex: 'name',
                sortable: false,
                hideable: false,

                renderer: function (value, b, record) {
                    return '<a href="registermappings/' + record.get('id') + '">' + value + '</a>';
                },
                flex: 1
            },
            {
                header: 'Reading type',
                dataIndex: 'readingType',
                flex: 1
            },
            {
                header: 'OBIS code',
                dataIndex: 'obisCode',
                flex: 1
            },
            {
                header: 'Type',
                dataIndex: 'type',
                flex: 1
            },
            {
                xtype: 'actioncolumn',
                tdCls: 'view',
                width: 24,
                items: [
                    {
                        icon: 'resources/images/gear-16x16.png',
                        tooltip: 'View',
                        handler: function (grid, rowIndex, colIndex, item, e) {
                            var menu = Ext.widget('menu', {
                                items: [
                                    {
                                        xtype: 'menuitem',
                                        text: 'Remove',
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    console.log('Remove');
                                                    this.fireEvent('remove', grid, grid.getSelectionModel().getSelection());
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
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: '{0} - {1} of {2} register types',
                displayMoreMsg: '{0} - {1} of more than {2} register types',
                emptyMsg: 'There are no register types to display'
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom'
            }
        ];

        this.callParent();
    }
});
