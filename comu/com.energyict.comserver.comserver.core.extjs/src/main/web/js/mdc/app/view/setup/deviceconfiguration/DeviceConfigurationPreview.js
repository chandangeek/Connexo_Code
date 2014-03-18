Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.deviceConfigurationPreview',
    itemId: 'deviceConfigurationPreview',
    requires: [
        'Mdc.model.DeviceConfiguration'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'panel',
            border: false,
            padding: '0 10 0 10',
            tbar: [
                {
                    xtype: 'component',
                    html: '<H4>'+Uni.I18n.translate('deviceconfiguration.noDeviceConfigurationSelected', 'MDC', 'No device configuration selected')+'</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>'+Uni.I18n.translate('deviceconfiguration.selectDeviceConfiguration', 'MDC', 'Select a device configuration to see its details')+'</H5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'deviceConfigurationPreviewForm',
            padding: '0 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>Device type</h4>',
                    itemId: 'deviceConfigurationPreviewTitle'
                },
                '->',
                {
                    icon: 'resources/images/gear-16x16.png',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    menu: {
                        items: [
                            {
                                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                itemId: 'editDeviceConfiguration',
                                action: 'editDeviceConfiguration'

                            },
                            {
                                text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                                itemId: 'deleteDeviceConfiguration',
                                action: 'deleteDeviceConfiguration'

                            },
                            {
                                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                                itemId: 'activateDeviceconfigurationMenuItem',
                                action: 'activateDeactivateDeviceConfiguration'

                            }
                        ]
                    }
                }
            ],
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
//                        align: 'stretch'
                    },
                    padding: '10 0 0 0',
                    items: [
                        {
                            xtype: 'container',
                            columnWidth: 0.5,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults:{
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
                                    itemId: 'deviceName'

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
                            defaults:{
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'fieldcontainer',
                                    columnWidth: 0.5,
                                    fieldLabel: Uni.I18n.translate('deviceconfiguration.dataSources', 'MDC', 'Data sources'),
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
                                                html: Uni.I18n.translate('deviceconfiguration.registers', 'MDC', 'registers')
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
                },
                {
                    xtype: 'toolbar',
                    docked: 'bottom',
                    border: false,
                    title: 'Bottom Toolbar',
                    items: [
                        '->',
                        {
                            xtype: 'component',
                            itemId: 'deviceConfigurationDetailsLink',
                            html: '' // filled in in Controller
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


