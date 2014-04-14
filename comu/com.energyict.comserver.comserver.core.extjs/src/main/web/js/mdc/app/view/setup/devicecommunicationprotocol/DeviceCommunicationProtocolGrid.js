Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationProtocolGrid',
    overflowY: 'auto',
    itemId: 'devicecommunicationprotocolgrid',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceCommunicationProtocolsPaged'
    ],
    store: 'DeviceCommunicationProtocolsPaged',
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('deviceCommunicationProtocols.name', 'MDC', 'Name'),
                dataIndex: 'name',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('deviceCommunicationProtocols.version', 'MDC', 'Version'),
                dataIndex: 'deviceProtocolVersion',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 2
            },
            {
                xtype: 'actioncolumn',
                iconCls: 'uni-centered-icon',
                tdCls: 'view',
                header: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                sortable: false,
                hideable: false,
                fixed: true,
                items: [
                    {
                        icon: '../mdc/resources/images/gear-16x16.png',
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
                displayMsg: Uni.I18n.translate('deviceCommunicationProtocols.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} protocols'),
                displayMoreMsg: Uni.I18n.translate('deviceCommumnicationProtocols.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} protocols'),
                emptyMsg: Uni.I18n.translate('deviceCommunicationProtocols.pagingtoolbartop.emptyMsg', 'MDC', 'There are no protocols to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('deviceCommunicationProtocols.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Protocols per page')
            }
        ];

        this.callParent();
    }
})
;
