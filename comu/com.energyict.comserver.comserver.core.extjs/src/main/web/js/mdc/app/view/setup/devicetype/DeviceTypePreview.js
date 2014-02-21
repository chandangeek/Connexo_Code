Ext.define('Mdc.view.setup.devicetype.DeviceTypePreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.deviceTypePreview',
    itemId: 'deviceTypePreview',
    requires: [
        'Mdc.model.DeviceType'
    ],
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
                    html: '<H4>'+Uni.I18n.translate('devicetype.noDeviceTypeSelected', 'MDC', 'No device type selected')+'</H4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<H5>'+Uni.I18n.translate('devicetype.selectDeviceType', 'MDC', 'Select a device type to see its details')+'</H5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'deviceTypePreviewForm',
            padding: '0 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>Device type</h4>',
                    itemId: 'deviceTypePreviewTitle'
                },
                '->',
                {
                    icon: 'resources/images/gear-16x16.png',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    menu: {
                        items: [
                            {
                                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                itemId: 'editDeviceType',
                                action: 'editDeviceType'

                            },
                            {
                                xtype: 'menuseparator'
                            },
                            {
                                text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                                itemId: 'deleteDeviceType',
                                action: 'deleteDeviceType'

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
                                    fieldLabel: Uni.I18n.translate('devicetype.name', 'MDC', 'Name'),
                                    itemId: 'deviceName'

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'communicationProtocolName',
                                    fieldLabel: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Device Communication protocol')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'canBeGateway',
                                    fieldLabel: Uni.I18n.translate('devicetype.canBeGateway', 'MDC', 'Device can be a gateway'),
                                    renderer: function (item) {
                                        return item? Uni.I18n.translate('general.yes', 'MDC', 'Yes'): Uni.I18n.translate('general.no', 'MDC', 'No');
                                    },
                                    readOnly: true
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'canBeDirectlyAddressable',
                                    fieldLabel: Uni.I18n.translate('devicetype.canBeDirectlyAddressable', 'MDC', 'Device can be directly addressable'),
                                    renderer: function (item) {
                                        return item? Uni.I18n.translate('general.yes', 'MDC', 'Yes'): Uni.I18n.translate('general.no', 'MDC', 'No');
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
                            defaults:{
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'fieldcontainer',
                                    columnWidth: 0.5,
                                    fieldLabel: Uni.I18n.translate('devicetype.dataSources', 'MDC', 'Data sources'),
                                    layout: {
                                        type: 'vbox',
                                        align: 'stretch'
                                    },
                                    items: [
                                        {
                                            xtype: 'component',
                                            name: 'registerCount',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: Uni.I18n.translate('devicetype.registers', 'MDC', 'Registers')
                                            },
                                            itemId: 'deviceTypeRegistersLink'
                                        },


                                        {
                                            xtype: 'component',
                                            name: 'loadProfileCount',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'loadprofiles')
                                            },
                                            itemId: 'deviceTypeLoadProfilesLink'

                                        },
                                        {
                                            xtype: 'component',
                                            name: 'logBookCount',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: Uni.I18n.translate('devicetype.logbooks', 'MDC', 'logbooks')
                                            },
                                            itemId: 'deviceTypeLogBooksLink'
                                        }
                                    ]
                                },

                                {
                                    xtype: 'fieldcontainer',
                                    columnWidth: 0.5,
                                    fieldLabel:  Uni.I18n.translate('devicetype.deviceConfigurationCount', 'MDC', 'Device configuration count'),
                                    layout: {
                                        type: 'vbox',
                                        align: 'stretch'
                                    },
                                    items: [
                                        {
                                            xtype: 'component',
                                            name: 'deviceConfigurationCount',
                                            autoEl: {
                                                tag: 'a',
                                                href: '#',
                                                html: Uni.I18n.translate('devicetype.deviceconfigurations', 'MDC', 'device configurations')
                                            },
                                            itemId: 'deviceConfigurationsLink'
                                        }
                                    ]
                                }


//                                {
//                                    xtype: 'displayfield',
//                                    name: 'deviceConfigurationCount',
//                                    fieldLabel: Uni.I18n.translate('devicetype.deviceConfigurationCount', 'MDC', 'Device configuration count'),
//                                    renderer: function (item, b) {
//                                        return '<a href="#' + item + '">' + item + ' ' + Uni.I18n.translate('devicetype.deviceconfigurations', 'MDC', 'device configurations')
//                                        + '</a>';
//                                    }
//                                }
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
                            itemId: 'deviceTypeDetailsLink',
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

