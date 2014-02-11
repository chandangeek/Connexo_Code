Ext.define('Mdc.view.setup.devicetype.DeviceTypeDetail', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceTypeDetail',
    itemId: 'deviceTypeDetail',
    autoScroll: true,
    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-wrapper',


    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

            items: [

                {
                    xtype: 'form',
                    border: false,
                    itemId: 'deviceTypeDetailForm',
                    padding: '10 10 0 10',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    tbar: [
                        {
                            xtype: 'component',
                            html: '<h4>' + I18n.translate('general.overview', 'MDC', 'Overview') + '</h4>',
                            itemId: 'deviceTypePreviewTitle'
                        },
                        {
                            xtype: 'component',
                            flex: 1
                        },
                        {
                            xtype: 'button',
                            text: I18n.translate('general.delete', 'MDC', 'Delete'),
                            itemId: 'deleteButtonFromDetails',
                            action: 'deleteDeviceType'
                        },
                        {
                            xtype: 'button',
                            text: I18n.translate('general.edit', 'MDC', 'Edit'),
                            itemId: 'editButtonFromDetails',
                            action: 'editDeviceType'
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
                                        labelWidth: 200
                                    },
                                    items: [
                                        {
                                            xtype: 'displayfield',
                                            name: 'name',
                                            fieldLabel: I18n.translate('devicetype.name', 'MDC', 'Name')
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'communicationProtocolName',
                                            fieldLabel: I18n.translate('devicetype.communicationProtocol', 'MDC', 'Device communication protocol')
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'deviceFunction',
                                            fieldLabel: I18n.translate('devicetype.deviceFunction', 'MDC', 'Device function')
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'canBeGateway',
                                            fieldLabel: I18n.translate('devicetype.canBeGateway', 'MDC', 'Device can be a gateway'),
                                            renderer: function (item) {
                                                return item ? I18n.translate('general.yes', 'MDC', 'Yes') : I18n.translate('general.no', 'MDC', 'No');
                                            },
                                            readOnly: true
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'canBeDirectlyAddressable',
                                            fieldLabel: I18n.translate('devicetype.canBeDirectlyAddressable', 'MDC', 'Device can be directly addressable'),
                                            renderer: function (item) {
                                                return item ? 'Yes' : 'No';
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
                                            xtype: 'displayfield',
                                            name: 'registerCount',
                                            fieldLabel: I18n.translate('devicetype.dataSources', 'MDC', 'Data sources'),
                                            renderer: function (item) {
                                                return '<a href="#' + item + '">' + item + ' ' + I18n.translate('devicetype.registers', 'MDC', 'registers') + '</a>';
                                            }
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'loadProfileCount',
                                            fieldLabel: ' ',
                                            renderer: function (item) {
                                                return '<a href="#' + item + '">' + item + ' ' + I18n.translate('devicetype.loadprofiles', 'MDC', 'load profiles') + '</a>';
                                            }
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'logBookCount',
                                            fieldLabel: ' ',
                                            renderer: function (item) {
                                                return '<a href="#' + item + '">' + item + ' ' + I18n.translate('devicetype.logbooks', 'MDC', 'logbooks') + '</a>';
                                            }
                                        },
                                        {
                                            xtype: 'displayfield',
                                            name: 'deviceConfigurationCount',
                                            fieldLabel: I18n.translate('devicetype.deviceConfigurationCount', 'MDC', 'Device configuration count'),
                                            renderer: function (item) {
                                                return '<a href="#' + item + '">' + item + ' ' + I18n.translate('devicetype.deviceconfigurations', 'MDC', 'device configurations') + '</a>';
                                            }
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
});


