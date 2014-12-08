Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationDetail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationDetail',
    itemId: 'deviceConfigurationDetail',
    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationsGrid',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationPreview',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu'
    ],
    deviceTypeId: null,
    deviceConfigurationId: null,

    content: [
        {
            xtype: 'panel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            ui: 'large',
            title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),

            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    defaults: {
                        xtype: 'container'
                    },
                    items: [
                        {
                            flex: 1
                        },
                        {
                            layout: {
                                type: 'vbox',
                                align: 'center',
                                pack: 'center'
                            },

                            items: [
                                {
                                    xtype: 'container',
                                    flex: 1
                                },
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                                    hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceConfiguration'),
                                    iconCls: 'x-uni-action-iconD',
                                    menu: {
                                        xtype: 'device-configuration-action-menu'
                                    }
                                },
                                {
                                    xtype: 'container',
                                    flex: 1
                                }
                            ]
                        }                    ]
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'deviceConfigurationDetailForm',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },

                    items: [
                        {
                            xtype: 'container',
                            layout: {
                                type: 'column',
                                align: 'stretch'
                            },
                            items: [
                                {
                                    xtype: 'container',
                                    columnWidth: 0.5,
                                    layout: {
                                        type: 'vbox',
                                        align: 'stretch'
                                    },
                                    defaults: {
                                        labelWidth: 250
                                    },
                                    items: [
                                        {
                                            xtype: 'displayfield',
                                            name: 'name',
                                            fieldLabel: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
                                            itemId: 'deviceName'

                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'description',
                                            fieldLabel: Uni.I18n.translate('deviceconfiguration.description', 'MDC', 'Description'),
                                            itemId: 'deviceConfigurationDescription'
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'active',
                                            fieldLabel: Uni.I18n.translate('deviceconfiguration.status', 'MDC', 'Status'),
                                            itemId: 'deviceConfigurationStatus',
                                            renderer: function (item) {
                                                return item ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                                            }
                                        },
                                        {
                                            xtype: 'displayfield',
                                            fieldLabel: Uni.I18n.translate('deviceconfiguration.isDirectlyAddressable', 'MDC', 'Directly addressable'),
                                            name: 'isDirectlyAddressable',
                                            renderer: function (value) {
                                                return value === true ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                                            }
                                        },
                                        {
                                            xtype: 'displayfield',
                                            fieldLabel: Uni.I18n.translate('deviceconfiguration.Gateway', 'MDC', 'Gateway'),
                                            name: 'canBeGateway',
                                            renderer: function (value) {
                                                var text,
                                                    record;

                                                if (value) {
                                                    record = this.up('#deviceConfigurationDetailForm').getRecord();
                                                    text = Uni.I18n.translate('general.yes', 'MDC', 'Yes');
                                                    if (record) {
                                                        text += ' (' + record.get('gatewayType') + ')';
                                                    }
                                                } else {
                                                    text = Uni.I18n.translate('general.no', 'MDC', 'No');
                                                }
                                                return text;
                                            }
                                        }
//                                {
//                                    xtype: 'displayfield',
//                                    name: 'communicationProtocolName',
//                                    fieldLabel: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Device Communication protocol')
//                                },
                                    ]
                                },
                                {
                                    xtype: 'container',
                                    columnWidth: 0.5,
                                    layout: {
                                        type: 'vbox',
                                        align: 'stretch'
                                    },
                                    defaults: {
                                        labelWidth: 250
                                    },
                                    items: [
                                        {
                                            xtype: 'fieldcontainer',
                                            columnWidth: 0.5,
                                            fieldLabel: Uni.I18n.translate('devicetype.dataSources', 'MDC', 'Data sources'),
                                            labelAlign: 'right',
                                            layout: {
                                                type: 'vbox'
                                            },
                                            items: [
                                                {
                                                    xtype: 'button',
                                                    name: 'registerCount',
                                                    text: Uni.I18n.translate('deviceconfig.registerconfigs', 'MDC', 'register configurations'),
                                                    ui: 'link',
                                                    itemId: 'deviceConfigurationDetailRegistersLink',
                                                    href: '#'
                                                },

                                                {
                                                    xtype: 'button',
                                                    name: 'loadProfileCount',
                                                    text: Uni.I18n.translate('deviceconfiguration.loadprofiles', 'MDC', 'load profiles'),
                                                    ui: 'link',
                                                    itemId: 'deviceConfigurationDetailLoadProfilesLink',
                                                    href: '#'
                                                },

                                                {
                                                    xtype: 'button',
                                                    name: 'logBookCount',
                                                    text: Uni.I18n.translate('deviceconfiguration.logbooks', 'MDC', 'logbooks'),
                                                    ui: 'link',
                                                    itemId: 'deviceConfigurationDetailLogBooksLink',
                                                    href: '#'
                                                }
                                            ]
                                        }
                                    ]
                                }

                            ]
                        }
                    ]
                }

            ]}
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigurationId,
                        toggle: 0
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});