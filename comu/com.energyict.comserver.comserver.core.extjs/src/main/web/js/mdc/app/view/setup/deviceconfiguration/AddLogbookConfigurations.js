Ext.define('Mdc.view.setup.deviceconfiguration.AddLogbookConfigurations', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-logbook-configurations',
    deviceTypeId: null,
    deviceConfigurationId: null,

    content: [
        {
            items: [
                {
                    xtype: 'panel',
                    ui: 'large',
                    itemId: 'addLogbookConfigurationTitle'
                },
                {
                    xtype: 'toolbar',
                    border: 0,
                    aling: 'left',
                    items: [
                        {
                            xtype: 'label',
                            name: 'LogBookCount',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            text: 'Manage logbooks',
                            action: 'manage',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/lbtadministration/logbook';
                                    }
                                }
                            }
                        }
                    ]
                },
                {
                    xtype: 'grid',
                    store: 'LogbookConfigurations',
                    height: 395,
                    selType: 'checkboxmodel',
                    selModel: {
                        checkOnly: true,
                        enableKeyNav: false,
                        showHeaderCheckbox: false
                    },
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
          &nbsp;&nbsp; - No logbook configuration have been defined yet.<br>"
                        }
                    ]
                },
                {
                    layout: 'hbox',
                    defaults: {
                        xtype: 'button'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            action: 'add',
                            ui: 'action'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            action: 'cancel',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/administration/devicetypes/' + this.up('add-logbook-configurations').deviceTypeId + '/deviceconfigurations/' + this.up('add-logbook-configurations').deviceConfigurationId + '/logbookconfigurations';
                                    }
                                }
                            }
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

