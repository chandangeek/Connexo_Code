Ext.define('Mdc.view.setup.devicetype.DeviceTypeDetail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTypeDetail',
    itemId: 'deviceTypeDetail',
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview',
        'Mdc.view.setup.devicetype.SideMenu'
    ],
    deviceTypeId: null,

    content: [
        {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [
                {
                    xtype: 'panel',
                    ui: 'large',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    defaults: {
                        xtype: 'container'
                    },
                    title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
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
                                    iconCls: 'x-uni-action-iconD',
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    menu: {
                                        xtype: 'device-type-action-menu'
                                    }
                                },
                                {
                                    xtype: 'container',
                                    flex: 1
                                }
                            ]
                        }]
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'deviceTypeDetailForm',
                    layout: {
                        type: 'vbox'
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
                                            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'deviceProtocolPluggableClass',
                                            fieldLabel: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Communication protocol')
                                        },
                                        {
                                            xtype: 'fieldcontainer',
                                            width: 500,
                                            fieldLabel: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                                            items: [
                                                {
                                                    itemId: 'details-device-life-cycle-link',
                                                    xtype: 'button',
                                                    ui: 'link',
                                                    href: 'javascript:void(0)'
                                                }
                                            ]
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'canBeGateway',
                                            fieldLabel: Uni.I18n.translate('devicetype.canBeGateway', 'MDC', 'Device can be a gateway'),
                                            renderer: function (item) {
                                                return item ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                                            },
                                            readOnly: true
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'canBeDirectlyAddressed',
                                            fieldLabel: Uni.I18n.translate('devicetype.canBeDirectlyAddressable', 'MDC', 'Device can be directly addressable'),
                                            renderer: function (item) {
                                                return item ? Uni.I18n.translate('general.yes', 'MDC', 'Yes') : Uni.I18n.translate('general.no', 'MDC', 'No');
                                            },
                                            readOnly: true
                                        }
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
                                            layout: {
                                                type: 'vbox'
                                            },
                                            defaults: {
                                                xtype: 'button',
                                                ui: 'link',
                                                href: 'javascript:void(0)'
                                            },
                                            items: [
                                                {
                                                    name: 'registerCount',
                                                    text: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
                                                    itemId: 'deviceTypeDetailRegistersLink'
                                                },

                                                {
                                                    name: 'loadProfileCount',
                                                    text: Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'Loadprofile types'),
                                                    itemId: 'deviceTypeDetailLoadProfilesLink'
                                                },

                                                {
                                                    name: 'logBookCount',
                                                    text: Uni.I18n.translate('devicetype.logbooks', 'MDC', 'Logbook types'),
                                                    itemId: 'deviceTypeDetailLogBooksLink'
                                                }
                                            ]
                                        },
                                        {
                                            xtype: 'fieldcontainer',
                                            columnWidth: 0.5,
                                            fieldLabel: Uni.I18n.translate('devicetype.deviceConfigurations', 'MDC', 'Device configurations'),
                                            layout: {
                                                type: 'vbox'
                                            },
                                            defaults: {
                                                xtype: 'button',
                                                ui: 'link',
                                                href: 'javascript:void(0)'
                                            },
                                            items: [
                                                {
                                                    name: 'deviceConfigurationCount',
                                                    text: Uni.I18n.translate('devicetype.deviceconfigurations', 'MDC', 'device configurations'),
                                                    itemId: 'deviceConfigurationsDetailLink'
                                                }
                                            ]
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
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        toggle: 0
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});


