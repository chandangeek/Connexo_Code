Ext.define('Mdc.view.setup.registergroup.RegisterGroupGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerGroupGrid',
    overflowY: 'auto',
    itemId: 'registerGroupGrid',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Mdc.store.RegisterGroups'
    ],
    store: 'RegisterGroups',
    //padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('registerGroup.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'actioncolumn',
                iconCls: 'uni-actioncolumn-gear',
                columnWidth: 32,
                header: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                items: [
                    {
                        icon: '../mdc/resources/images/masterActions.png',
                        handler: function (grid, rowIndex, colIndex, item, e, record, row) {
                            var menu = Ext.widget('menu', {
                                items: [
                                    {
                                        xtype: 'menuitem',
                                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    this.fireEvent('editItem', record);
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
                displayMsg: Uni.I18n.translate('registerGroups.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register groups'),
                displayMoreMsg: Uni.I18n.translate('registerGroups.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register groups'),
                emptyMsg: Uni.I18n.translate('registerGroups.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register groups to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: Uni.I18n.translate('registerGroup.add', 'MDC', 'Add register group'),
                        itemId: 'createRegisterGroup',
                        xtype: 'button',
                        action: 'createRegisterGroup'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registerGroups.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register groups per page')
            }
        ];

        this.callParent();
    }
})
;
