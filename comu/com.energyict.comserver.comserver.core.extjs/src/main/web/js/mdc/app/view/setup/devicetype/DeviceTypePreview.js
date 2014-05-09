Ext.define('Mdc.view.setup.devicetype.DeviceTypePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    //margins: '0 10 10 10',
    alias: 'widget.deviceTypePreview',
    itemId: 'deviceTypePreview',
    requires: [
        'Mdc.model.DeviceType'
    ],

    title: 'Details',
    tools: [
        {
            xtype: 'button',
            icon: '../mdc/resources/images/actionsDetail.png',
            //glyph: 71,
            text: 'Action',
            menu: [
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

    ],
    items: {
        xtype: 'form',
        border: false,
        itemId: 'deviceTypePreviewForm',
        //padding: '10 10 0 10',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                xtype: 'container',
                layout: {
                    type: 'column'
//                        align: 'stretch'
                },
                //padding: '10 0 0 0',
                items: [
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox'//,
                            //align: 'stretch'
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
                                name: 'communicationProtocolName',
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
                            type: 'vbox'//,
                            //align: 'stretch'
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
                                            html: Uni.I18n.translate('devicetype.registers', 'MDC', 'Register types')
                                        },
                                        itemId: 'deviceTypeRegistersLink'
                                    },


                                    {
                                        xtype: 'component',
                                        name: 'loadProfileCount',
                                        cls: 'x-form-display-field',
                                        autoEl: {
                                            tag: 'a',
                                            href: '#',
                                            html: Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'loadprofile types')
                                        },
                                        itemId: 'deviceTypeLoadProfilesLink'

                                    },
                                    {
                                        xtype: 'component',
                                        name: 'logBookCount',
                                        cls: 'x-form-display-field',
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
                                fieldLabel: Uni.I18n.translate('devicetype.deviceConfigurationCount', 'MDC', 'Device configuration count'),
                                layout: {
                                    type: 'vbox'
                                },
                                items: [
                                    {
                                        xtype: 'component',
                                        name: 'deviceConfigurationCount',
                                        cls: 'x-form-display-field a',
                                        autoEl: {
                                            tag: 'a',
                                            href: '#',
                                            html: Uni.I18n.translate('devicetype.deviceconfigurations', 'MDC', 'device configurations')
                                        },
                                        itemId: 'deviceConfigurationsLink'
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
                        itemId: 'deviceTypeDetailsLink',
                        html: '' // filled in in Controller
                    }

                ]
            }
        ]
    },
    // todo: set empty text
    emptyText: '<h3>' + Uni.I18n.translate('devicetype.noDeviceTypeSelected', 'MDC', 'No device type selected') + '</h3><p>' + Uni.I18n.translate('devicetype.selectDeviceType', 'MDC', 'Select a device type to see its details') + '</p>'
});
