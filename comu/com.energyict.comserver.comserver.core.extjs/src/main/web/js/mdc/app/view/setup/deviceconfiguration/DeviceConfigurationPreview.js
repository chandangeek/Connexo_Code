Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceConfigurationPreview',
    itemId: 'deviceConfigurationPreview',
    requires: [
        'Mdc.model.DeviceConfiguration',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationActionMenu'
    ],
    frame: true,
    title: "Details",


    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'device-configuration-action-menu'
            }
        }
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'panel',
            border: false,
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('deviceconfiguration.noDeviceConfigurationSelected', 'MDC', 'No device configuration selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('deviceconfiguration.selectDeviceConfiguration', 'MDC', 'Select a device configuration to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            itemId: 'deviceConfigurationPreviewForm',
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
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
                                    itemId: 'deviceName'

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
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.dataSources', 'MDC', 'Data sources'),
                                    labelAlign: 'right',
                                    layout: {
                                        type: 'vbox'
                                    },
                                    items: [

                                        {
                                            xtype: 'button',
                                            name: 'registerCount',
                                            text: Uni.I18n.translate('deviceconfiguration.registers', 'MDC', 'register configurations'),
                                            ui: 'link',
                                            itemId: 'deviceConfigurationRegistersLink',
                                            href: '#'
                                        },

                                        {
                                            xtype: 'button',
                                            name: 'loadProfileCount',
                                            text: Uni.I18n.translate('deviceconfiguration.loadprofiles', 'MDC', 'loadprofiles'),
                                            ui: 'link',
                                            itemId: 'deviceConfigurationLoadProfilesLink',
                                            href: '#'
                                        },

                                        {
                                            xtype: 'button',
                                            name: 'logBookCount',
                                            text: Uni.I18n.translate('deviceconfiguration.logbooks', 'MDC', 'logbooks'),
                                            ui: 'link',
                                            itemId: 'deviceConfigurationLogBooksLink',
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
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
