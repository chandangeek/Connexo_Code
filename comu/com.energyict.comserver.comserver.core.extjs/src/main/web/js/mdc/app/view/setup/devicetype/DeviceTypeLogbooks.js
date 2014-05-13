Ext.define('Mdc.view.setup.devicetype.DeviceTypeLogbooks', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-logbooks',
    deviceTypeId: null,
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypeMenu'
    ],
    content: [
        {
            items: [
                {
                    xtype: 'panel',
                    ui: 'large',
                    itemId: 'deviceTypeLogbookTitle'
                },
                {
                    xtype: 'toolbar',
                    border: 0,
                    aling: 'left',
                    items: [
                        {
                            xtype: 'container',
                            name: 'LogBookCount',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            text: 'Add logbook type',
                            action: 'add',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#setup/devicetypes/' + this.up('device-type-logbooks').deviceTypeId + '/logbooktypes/add';
                                    }
                                }
                            }
                        }
                    ]
                },
                {
                    xtype: 'grid',
                    height: 395,
                    store: 'LogbookTypes',
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
                                dataIndex: 'obisCode',
                                flex: 5
                            },
                            {
                                header: 'Actions',
                                xtype: 'actioncolumn',
                                iconCls: 'x-uni-action-icon',
                                flex: 1
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
                            html: "<h3>No logbook types found</h3><br>\
          There are no logbook types. This could be because:<br>\
          &nbsp;&nbsp; - No logbook types have been defined yet.<br>\
          &nbsp;&nbsp; - No logbook types comply to the filter.<br><br>\
          Possible steps:<br><br>"
                        },
                        {
                            xtype: 'button',
                            text: 'Add logbook type',
                            action: 'add',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#setup/devicetypes/' + this.up('device-type-logbooks').deviceTypeId + '/logbooktypes/add';
                                    }
                                }
                            }
                        }
                    ]
                },
                {
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
                            iconCls: 'x-uni-action-iconA',
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
            ]
        }
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'deviceTypeMenu',
                itemId: 'stepsMenu',
                deviceTypeId: this.deviceTypeId,
                toggle: 3
            }
        ];
        this.callParent(arguments);
    }
});
