Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationLogbooks', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-configuration-logbooks',
    deviceConfigurationId: null,
    deviceTypeId: null,
    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.view.container.PreviewContainer',
        'Mdc.view.setup.deviceconfiguration.ActionMenu',
        'Uni.grid.column.Action'
    ],
    content: [
        {
            xtype: 'panel',
            title: Uni.I18n.translate('deviceconfiguration.logbookConfiguration', 'MDC', 'Logbook configuration'),
            ui: 'large',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'grid',
                        store: 'LogbookConfigurations',
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
                                    dataIndex: 'overruledObisCode',
                                    flex: 5
                                },
                                {
                                    xtype: 'uni-actioncolumn',
                                    items: 'Mdc.view.setup.deviceconfiguration.ActionMenu'
                                }
                            ]
                        },
                        dockedItems: [
                            {
                                xtype: 'pagingtoolbartop',
                                store: 'LogbookConfigurations',
                                dock: 'top',
                                displayMsg: Uni.I18n.translate('deviceconfiguration.logbookConfiguration.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} logbook configurations'),
                                displayMoreMsg: Uni.I18n.translate('deviceconfiguration.logbookConfiguration.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} logbook configurations'),
                                emptyMsg: Uni.I18n.translate('deviceconfiguration.logbookConfiguration.pagingtoolbartop.emptyMsg', 'MDC', 'There are no logbook configurations to display'),
                                items: [
                                    '->',
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('deviceconfiguration.addLogbookConfiguration', 'MDC', 'Add logbook configuration'),
                                        action: 'add',
                                        listeners: {
                                            click: {
                                                fn: function () {
                                                    window.location.href = '#/administration/devicetypes/' + this.up('device-configuration-logbooks').deviceTypeId + '/deviceconfigurations/' + this.up('device-configuration-logbooks').deviceConfigurationId + '/logbookconfigurations/add';
                                                }
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                xtype: 'pagingtoolbarbottom',
                                dock: 'bottom',
                                store: 'LogbookConfigurations',
                                itemsPerPageMsg: Uni.I18n.translate('logbookconfiguration.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Logbook configurations per page')
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
                                        html: '<b>' + Uni.I18n.translate('deviceconfiguration.logbookConfiguration.empty.title', 'MDC', 'No logbook configuration found') + '</b><br>' +
                                            Uni.I18n.translate('deviceconfiguration.logbookConfiguration.empty.detail', 'MDC', 'There are no logbooks. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('deviceconfiguration.logbookConfiguration.empty.list.item1', 'MDC', 'No logbook configuration have been defined yet') + '</li>' +
                                            Uni.I18n.translate('deviceconfiguration.logbookConfiguration.empty.list.item2', 'MDC', 'No logbook configuration comply to the filter') + '</li></lv><br>' +
                                            Uni.I18n.translate('deviceconfiguration.logbookConfiguration.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('deviceconfiguration.addLogbookConfiguration', 'MDC', 'Add logbook configuration'),
                                        action: 'add',
                                        listeners: {
                                            click: {
                                                fn: function () {
                                                    window.location.href = '#/administration/devicetypes/' + this.up('device-configuration-logbooks').deviceTypeId + '/deviceconfigurations/' + this.up('device-configuration-logbooks').deviceConfigurationId + '/logbookconfigurations/add';
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
                        title: 'Details',
                        name: 'details',
                        frame: true,
                        tools: [
                            {
                                xtype: 'button',
                                text: 'Actions',
                                iconCls: 'x-uni-action-iconD',
                                menu: {
                                    xtype: 'device-logbook-action-menu'
                                }
                            }
                        ],
                        items: [
                            {
                                xtype: 'form',
                                name: 'logbookConfigurationDetails',
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
                                                name: 'name',
                                                labelWidth: 160
                                            },
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: 'Overruled OBIS code',
                                                name: 'overruledObisCode',
                                                labelWidth: 160
                                            }
                                        ]
                                    },
                                    {
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                fieldLabel: 'Logbook OBIS code',
                                                labelWidth: 160,
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
                        xtype: 'deviceConfigurationMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigurationId,
                        toggle: 3
                    }
                ]
            }
        ];
        this.callParent(arguments);
        Ext.data.StoreManager.lookup('LogbookConfigurations').load();
    }
});

