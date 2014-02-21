Ext.define('Mdc.view.setup.register.RegisterMappingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerMappingsGrid',
    overflowY: 'auto',
    deviceTypeId: null,
    itemId: 'registermappinggrid',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterTypes'
    ],
    store: 'RegisterTypes',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('registerMappings.name', 'MDC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                renderer: function (value, b, record) {
                    return '<a href="#setup/devicetypes/' + me.deviceTypeId + '/registertypes/' + record.get('id') + '">' + value + '</a>';
                },
                flex: 2
            },
            {
                xtype: 'actioncolumn',
                renderer: function (value, metaData, record) {
                    return '<div style="float:left; font-size: 13px; line-height: 1em;">'
                        + record.get('mrid') + '&nbsp' + '&nbsp'
                        + '</div>'
                },
                header: Uni.I18n.translate('registerMappings.readingType', 'MDC', 'Reading type'),
                items: [
                    {
                        icon: 'resources/images/gear-16x16.png',
                        tooltip: 'Reading type info',
                        handler: function (grid, rowIndex, colIndex, item, e) {
                            var record = grid.getStore().getAt(rowIndex);
                            this.fireEvent('showReadingTypeInfo', record);
                        }
                    }
                ],
                flex: 2,
                tdCls: 'view',
                sortable: false,
                hideable: false
            },
            {
                header: Uni.I18n.translate('registerMappings.obisCode', 'MDC', 'OBIS code'),
                dataIndex: 'obisCode',
                sortable: false,
                hideable: false,
                flex: 1
            },
            {
                header: Uni.I18n.translate('registerMappings.type', 'MDC', 'Type'),
                dataIndex: 'measurementKind',
                sortable: false,
                hideable: false,
                flex: 1
            },
            {
                xtype: 'actioncolumn',
                tdCls: 'view',
                header : Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                sortable: false,
                hideable: false,
                items: [
                    {
                        icon: 'resources/images/gear-16x16.png',
                        handler: function (grid, rowIndex, colIndex, item, e) {
                            var menu = Ext.widget('menu', {
                                items: [
                                    {
                                        xtype: 'menuitem',
                                        text: Uni.I18n.translate('registerMappings.remove', 'MDC', 'Remove'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function () {
                                                    console.log('Remove');
                                                    this.fireEvent('removeItem', grid, grid.getSelectionModel().getSelection(), me.deviceTypeId);
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
                displayMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register types'),
                displayMoreMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register types'),
                emptyMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register types to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {

                        text: Uni.I18n.translate('registerMapping.addRegisterMapping', 'MDC', 'Add register types'),
                        itemId: 'addRegisterMappingBtn',
                        xtype: 'button',
                        href: '#setup/devicetypes/' + this.deviceTypeId + '/registertypes/add',
                        hrefTarget: '_self',
                        action: 'addRegisterMapping'
                    },
                    {
                        text: Uni.I18n.translate('general.bulkAction', 'MDC', 'Bulk action'),
                        itemId: 'registerMappingsBulkAction',
                        xtype: 'button'
                    }
                ]
            }
        ];

        this.callParent();
    }
});
