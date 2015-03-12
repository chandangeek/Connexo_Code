Ext.define('Mdc.view.setup.devicetype.DeviceTypePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceTypePreview',
    itemId: 'deviceTypePreview',

    requires: [
        'Mdc.model.DeviceType',
        'Mdc.view.setup.devicetype.DeviceTypeActionMenu'
    ],

    title: 'Details',

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'device-type-action-menu'
            }
        }
    ],


    items: {
        xtype: 'form',
        border: false,
        itemId: 'deviceTypePreviewForm',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                xtype: 'container',
                layout: {
                    type: 'column'
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
                                fieldLabel: Uni.I18n.translate('devicetype.name', 'MDC', 'Name'),
                                itemId: 'deviceName'

                            },
                            {
                                xtype: 'displayfield',
                                name: 'deviceProtocolPluggableClass',
                                fieldLabel: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Communication protocol')
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
                                labelAlign: 'right',
                                layout: {
                                    type: 'vbox'
                                },
                                defaults: {
                                    xtype: 'button',
                                    ui: 'link',
                                    href: '#',
                                    style: 'text-decoration: underline'
                                },
                                items: [
                                    {
                                        name: 'registerCount',
                                        text: Uni.I18n.translate('devicetype.registers', 'MDC', 'Register types'),
                                        itemId: 'deviceTypeRegistersLink'
                                    },

                                    {
                                        name: 'loadProfileCount',
                                        text: Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'loadprofile types'),
                                        itemId: 'deviceTypeLoadProfilesLink'
                                    },

                                    {
                                        name: 'logBookCount',
                                        text: Uni.I18n.translate('devicetype.logbooks', 'MDC', 'logbooks'),
                                        itemId: 'deviceTypeLogBooksLink'
                                    }
                                ]
                            },

                            {
                                xtype: 'fieldcontainer',
                                columnWidth: 0.5,
                                fieldLabel: Uni.I18n.translate('devicetype.deviceConfigurations', 'MDC', 'Device configurations'),
                                labelAlign: 'right',
                                layout: {
                                    type: 'vbox'
                                },
                                defaults: {
                                    xtype: 'button',
                                    ui: 'link',
                                    href: '#',
                                    style: 'text-decoration: underline'
                                },
                                items: [
                                    {
                                        name: 'deviceConfigurationCount',
                                        text: Uni.I18n.translate('devicetype.deviceconfigurations', 'MDC', 'device configurations'),
                                        itemId: 'deviceConfigurationsLink'
                                    }
                                ]
                            }
                        ]
                    }

                ]
            }
        ]
    },
    // todo: set empty text
    emptyText: '<h3>' + Uni.I18n.translate('devicetype.noDeviceTypeSelected', 'MDC', 'No device type selected') + '</h3><p>' + Uni.I18n.translate('devicetype.selectDeviceType', 'MDC', 'Select a device type to see its details') + '</p>'
});
