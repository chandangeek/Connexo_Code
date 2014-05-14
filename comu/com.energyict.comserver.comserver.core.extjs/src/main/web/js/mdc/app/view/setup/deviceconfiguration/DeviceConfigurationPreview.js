Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceConfigurationPreview',
    itemId: 'deviceConfigurationPreview',
    requires: [
        'Mdc.model.DeviceConfiguration'
    ],
    frame: true,
    title: "Details",
    tools: [
        {
            xtype: 'button',
            icon: '../mdc/resources/images/actionsDetail.png',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            menu: {
                items: [
                    {
                        text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                        itemId: 'activateDeviceconfigurationMenuItem',
                        action: 'activateDeactivateDeviceConfiguration'
                    },
                    {
                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                        itemId: 'editDeviceConfiguration',
                        action: 'editDeviceConfiguration'
                    },
                    {
                        text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                        itemId: 'deleteDeviceConfiguration',
                        action: 'deleteDeviceConfiguration'
                    }
                ]
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
                    //padding: '10 0 0 0',
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
                                            xtype: 'component',
                                            name: 'registerCount',
                                            cls: 'x-form-display-field',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: Uni.I18n.translate('deviceconfiguration.registers', 'MDC', 'register configurations')
                                            },
                                            itemId: 'deviceConfigurationRegistersLink'
                                        },


                                        {
                                            xtype: 'component',
                                            name: 'loadProfileCount',
                                            cls: 'x-form-display-field',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: Uni.I18n.translate('deviceconfiguration.loadprofiles', 'MDC', 'loadprofiles')
                                            },
                                            itemId: 'deviceConfigurationLoadProfilesLink'

                                        },
                                        {
                                            xtype: 'component',
                                            name: 'logBookCount',
                                            cls: 'x-form-display-field',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: Uni.I18n.translate('deviceconfiguration.logbooks', 'MDC', 'logbooks')
                                            },
                                            itemId: 'deviceConfigurationLogBooksLink'
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
