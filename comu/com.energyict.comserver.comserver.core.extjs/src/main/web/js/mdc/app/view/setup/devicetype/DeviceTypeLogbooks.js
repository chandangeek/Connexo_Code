Ext.define('Mdc.view.setup.devicetype.DeviceTypeLogbooks', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-logbooks',
    deviceTypeId: null,
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypeMenu',
        'Uni.view.container.PreviewContainer',
        'Mdc.view.setup.deviceconfiguration.ActionMenu',
        'Uni.grid.column.Action'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'deviceTypeLogbookPanel',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'grid',
                        store: 'LogbookTypes',
                        columns: {
                            defaults: {
                                sortable: false,
                                menuDisabled: true
                            },
                            items: [
                                {
                                    header: 'Name',
                                    dataIndex: 'name',
                                    flex: 5
                                },
                                {
                                    header: 'OBIS code',
                                    dataIndex: 'obisCode',
                                    flex: 5
                                },
                                {
                                    xtype: 'uni-actioncolumn',
                                    items: 'Mdc.view.setup.devicetype.ActionMenu'
                                }
                            ]
                        },
                        dockedItems: [
                            {
                                xtype: 'pagingtoolbartop',
                                store: 'LogbookTypes',
                                dock: 'top',
                                displayMsg: Uni.I18n.translate('logbooktype.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} logbook types'),
                                displayMoreMsg: Uni.I18n.translate('logbooktype.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} logbook types'),
                                emptyMsg: Uni.I18n.translate('logbooktype.pagingtoolbartop.emptyMsg', 'MDC', 'There are no logbook types to display'),
                                items: [
                                    '->',
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('logbooktype.addLogbookType', 'MDC', 'Add logbook type'),
                                        action: 'add',
                                        listeners: {
                                            click: {
                                                fn: function () {
                                                    window.location.href = '#/administration/devicetypes/' + this.up('device-type-logbooks').deviceTypeId + '/logbooktypes/add';
                                                }
                                            }
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    emptyComponent: {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        minHeight: 20,
                        items: [
                            {
                                xtype: 'image',
                                margin: '0 10 0 0',
                                src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: '<b>' + Uni.I18n.translate('logbooktype.empty.title', 'MDC', 'No logbook types found') + '</b><br>' +
                                            Uni.I18n.translate('logbooktype.empty.detail', 'MDC', 'There are no logbooks. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('logbooktype.empty.list.item1', 'MDC', 'No logbook types have been defined yet') + '</li>' +
                                            Uni.I18n.translate('logbooktype.empty.list.item2', 'MDC', 'No logbook types comply to the filter') + '</li></lv><br>' +
                                            Uni.I18n.translate('logbooktype.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('logbooktype.addLogbookType', 'MDC', 'Add logbook type'),
                                        action: 'add',
                                        listeners: {
                                            click: {
                                                fn: function () {
                                                    window.location.href = '#/administration/devicetypes/' + this.up('device-type-logbooks').deviceTypeId + '/logbooktypes/add';
                                                }
                                            }
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'panel',
                        height: 110,
                        title: 'Details',
                        name: 'details',
                        frame: true,
                        hidden: true,
                        tools: [
                            {
                                xtype: 'button',
                                text: 'Actions',
                                iconCls: 'x-uni-action-iconD',
                                menu: {
                                    xtype: 'device-type-logbook-action-menu'
                                }
                            }
                        ],
                        items: [
                            {
                                xtype: 'form',
                                name: 'logbookTypeDetails',
                                layout: 'column',
                                defaults: {
                                    xtype: 'container',
                                    layout: 'form',
                                    columnWidth: 0.5
                                },
                                items: [
                                    {
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: 'Name',
                                                name: 'name'
                                            }
                                        ]
                                    },
                                    {
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: 'OBIS code',
                                                name: 'obisCode'
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        toggle: 3
                    }
                ]
            }
        ];
        this.callParent(arguments);
        Ext.data.StoreManager.lookup('LogbookTypes').load();
    }
});
