Ext.define('Mdc.view.setup.devicetype.DeviceTypeLogbooks', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-logbooks',

    deviceTypeId: null,

    requires: [
        'Mdc.view.setup.devicetype.DeviceTypeMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceconfiguration.ActionMenu',
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.form.field.ObisDisplay'
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
                                    xtype: 'obis-column',
                                    dataIndex: 'obisCode'
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
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('logbooktype.empty.title', 'MDC', 'No logbook types found'),
                        reasons: [
                            Uni.I18n.translate('logbooktype.empty.list.item1', 'MDC', 'No logbook types have been defined yet.'),
                            Uni.I18n.translate('logbooktype.empty.list.item2', 'MDC', 'No logbook types comply to the filter.')
                        ],
                        stepItems: [
                            {
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
                                                xtype: 'obis-displayfield',
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
