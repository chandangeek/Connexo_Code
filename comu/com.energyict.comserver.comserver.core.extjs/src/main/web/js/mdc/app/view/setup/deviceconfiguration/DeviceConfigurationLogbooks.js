Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationLogbooks', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-configuration-logbooks',
    deviceConfigurationId: null,
    deviceTypeId: null,
    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu'
    ],
    content: [
        {
            xtype: 'panel',
            title: 'Logbook configuration',
            ui: 'large',
            items: [
                {
                    xtype: 'toolbar',
                    aling: 'left',
                    items: [
                        {
                            xtype: 'container',
                            name: 'LogBookCount',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            text: 'Add logbook configuration',
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
                    xtype: 'grid',
                    height: 395,
                    store: 'LogbookConfigurations',
                    forceFit: true,
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
                    }
                },
                {
                    xtype: 'panel',
                    hidden: true,
                    height: 200,
                    items: [
                        {
                            xtype: 'panel',
                            html: "<h3>No logbook configuration found</h3><br>\
          There are no logbooks. This could be because:<br>\
          &nbsp;&nbsp; - No logbook configuration have been defined yet.<br>\
          &nbsp;&nbsp; - No logbook configuration comply to the filter.<br><br>\
          Possible steps:<br><br>"
                        },
                        {
                            xtype: 'button',
                            text: 'Add logbook configuration',
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
                    xtype: 'panel',
                    title: 'Details',
                    name: 'details',
                    frame: true,
                    hidden: true,
                    tools: [
                        {
                            xtype: 'button',
                            text: 'Actions',
                            iconCls: 'x-uni-action-iconA',
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
    }
});

