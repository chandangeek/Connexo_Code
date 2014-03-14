Ext.define('Mdc.view.setup.devicetype.DeviceConfigurationDetail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationDetail',
    itemId: 'deviceConfigurationDetail',
    autoScroll: true,
    requires: [
        'Mdc.view.setup.DeviceConfiguration.DeviceConfigurationsGrid',
        'Mdc.view.setup.DeviceConfiguration.DeviceConfigurationPreview'
    ],

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [
                {
                    xtype: 'breadcrumbTrail',
                    region: 'north',
                    padding: 6
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'deviceConfigurationDetailForm',
                    padding: '10 10 0 10',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    tbar: [
                        {
                            xtype: 'component',
                            html: '<h1>' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</h1>',
                            itemId: 'deviceConfigurationPreviewTitle'
                        },
                        {
                            xtype: 'component',
                            flex: 1
                        },
                        '->',
                        {
                            icon: '../mdc/resources/images/gear-16x16.png',
                            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                            menu: {
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                        itemId: 'editDeviceConfiguration',
                                        action: 'editDeviceConfiguration'

                                    },
                                    {
                                        xtype: 'menuseparator'
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
                                                    itemId: 'deviceConfigurationDetailRegistersLink'
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
                                                    itemId: 'deviceConfigurationDetailLoadProfilesLink'

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
                                                    itemId: 'deviceConfigurationDetailLogBooksLink'
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
        this.callParent(arguments);
    }
})
;



