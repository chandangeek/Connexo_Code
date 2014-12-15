Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationEdit',
    itemId: 'deviceConfigurationEdit',
    edit: false,
    isEdit: function () {
        return this.edit
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'deviceConfigurationEditCreateTitle',
                cls: 'content-container',
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
                                items: [
                                    {
                                        xtype: 'form',
                                        border: false,
                                        width: 800,
                                        itemId: 'deviceConfigurationEditForm',
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        defaults: {
                                            labelWidth: 250
                                        },
                                        items: [
                                            {
                                                xtype: 'textfield',
                                                name: 'name',
                                                msgTarget: 'under',
                                                required: true,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
                                                itemId: 'editDeviceConfigurationNameField',
                                                maxLength: 80,
                                                enforceMaxLength: true
                                            },
                                            {
                                                xtype: 'textareafield',
                                                name: 'description',
                                                height: 100,
                                                msgTarget: 'under',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.description', 'MDC', 'Description'),
                                                itemId: 'editDeviceConfigurationDescriptionField'
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                columnWidth: 0.5,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.isDirectlyAddressable', 'MDC', 'Directly addressable'),
                                                required: true,
                                                layout: {
                                                    type: 'hbox'
                                                },
                                                items: [
                                                    {
                                                        xtype: 'radiogroup',
                                                        itemId: 'directlyAddressableCombo',
                                                        columns: 1,
                                                        vertical: true,
                                                        items: [
                                                            {
                                                                name: "isDirectlyAddressable",
                                                                boxLabel: '<b>' + Uni.I18n.translate('general.yes', 'MDC', 'Yes') + '</b>',
                                                                inputValue: true,
                                                                checked: true
                                                            },
                                                            {
                                                                name: "isDirectlyAddressable",
                                                                boxLabel: '<b>' + Uni.I18n.translate('general.no', 'MDC', 'No') + '</b>',
                                                                inputValue: false,
                                                                checked: false
                                                            }
                                                        ]
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                fieldLabel: ' ',
                                                padding: '-12 0 15 0',
                                                itemId: 'addressableMessage',
                                                items: [
                                                    {
                                                        xtype: 'container',
                                                        html: '<span style="color: grey;padding: 0 0 0 23px;">' + Uni.I18n.translate('deviceconfiguration.directlyAddressable.description', 'MDC', 'If a device is directly addressable, a connection can be made to this device') + '</span>'
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                columnWidth: 0.5,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.deviceIsGateway', 'MDC', 'Device is a gateway'),
                                                required: true,
                                                layout: {
                                                    type: 'hbox'
                                                },
                                                hidden: false,
                                                items: [
                                                    {
                                                        xtype: 'radiogroup',
                                                        itemId: 'deviceIsGatewayCombo',
                                                        columns: 1,
                                                        vertical: true,
                                                        items: [
                                                            {
                                                                name: "canBeGateway",
                                                                boxLabel: '<b>' + Uni.I18n.translate('general.yes', 'MDC', 'Yes') + '</b>',
                                                                inputValue: true,
                                                                checked: false
                                                            },
                                                            {
                                                                name: "canBeGateway",
                                                                boxLabel: '<b>' + Uni.I18n.translate('general.no', 'MDC', 'No') + '</b>',
                                                                inputValue: false,
                                                                checked: true
                                                            }
                                                        ]
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                fieldLabel: ' ',
                                                padding: '-12 0 0 0',
                                                itemId: 'gatewayMessage',
                                                items: [
                                                    {
                                                        xtype: 'container',
                                                        html: '<span style="color: grey;padding: 0 0 0 23px;">' + Uni.I18n.translate('deviceconfiguration.deviceIsGateway.description', 'MDC', 'Gateways can be used as a connection to slave devices') + '</span>'
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                columnWidth: 0.5,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.typeOfGateway', 'MDC', 'Type of gateway'),
                                                itemId: 'typeOfGatewayComboContainer',
                                                padding: '15 0 0 0',
                                                required: true,
                                                layout: {
                                                    type: 'hbox'
                                                },
                                                hidden: true,
                                                items: [
                                                    {
                                                        xtype: 'radiogroup',
                                                        itemId: 'typeOfGatewayCombo',
                                                        columns: 1,
                                                        vertical: true,
                                                        items: [
                                                            {
                                                                name: "gatewayType",
                                                                boxLabel: '<b>' + Uni.I18n.translate('deviceconfiguration.HAN', 'MDC', 'HAN (Home Area Network)') + '</b>',
                                                                afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('deviceconfiguration.HAN.description', 'MDC', 'Connects a small number of slave devices in a home environment') + '</span>',
                                                                padding: '0 0 10 0',
                                                                inputValue: 'HAN',
                                                                checked: true
                                                            },
                                                            {
                                                                name: "gatewayType",
                                                                boxLabel: '<b>' + Uni.I18n.translate('deviceconfiguration.LAN', 'MDC', 'LAN (Local Area Network)') + '</b>',
                                                                afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('deviceconfiguration.LAN.description', 'MDC', 'Connects a large number of slave devices in a local environment') + '</span>',
                                                                inputValue: 'LAN',
                                                                checked: false
                                                            }
                                                        ]
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                ui: 'actions',
                                                fieldLabel: '&nbsp',
                                                layout: {
                                                    type: 'hbox'
                                                },
                                                items: [
                                                    {
                                                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                                        xtype: 'button',
                                                        ui: 'action',
                                                        action: 'createAction',
                                                        itemId: 'createEditButton'
                                                    },
                                                    {
                                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                                        xtype: 'button',
                                                        ui: 'link',
                                                        itemId: 'cancelLink',
                                                        href: '#/administration/devicetypes/'
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
        ];
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editDeviceConfiguration';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createDeviceConfiguration';
        }
        this.down('#cancelLink').href = this.returnLink;
    }

});
