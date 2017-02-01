/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationEdit',
    itemId: 'deviceConfigurationEdit',
    edit: false,
    requires: [
        'Uni.util.FormErrorMessage'
    ],
    returnLink: '#/administration/devicetypes/',
    isEdit: function () {
        return this.edit
    },

    initComponent: function () {
        var me = this;

        me.content = [
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
                                                xtype: 'uni-form-error-message',
                                                itemId: 'mdc-device-config-form-errors',
                                                name: 'errors',
                                                margin: '0 0 10 0',
                                                hidden: true
                                            },
                                            {
                                                xtype: 'textfield',
                                                name: 'name',
                                                msgTarget: 'under',
                                                required: true,
                                                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                                itemId: 'editDeviceConfigurationNameField',
                                                maxLength: 80,
                                                enforceMaxLength: true,
                                                listeners: {
                                                    afterrender: function (field) {
                                                        field.focus(false, 200);
                                                    }
                                                }
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
                                                itemId: 'fld-device-config-addressable',
                                                columnWidth: 0.5,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.isDirectlyAddressable', 'MDC', 'Directly addressable'),
                                                required: true,
                                                layout: 'fit',
                                                items: [
                                                    {
                                                        xtype: 'radiogroup',
                                                        itemId: 'directlyAddressableCombo',
                                                        name: "isDirectlyAddressable",
                                                        msgTarget: 'under',
                                                        combineErrors: true,
                                                        columns: 1,
                                                        vertical: true,
                                                        items: [
                                                            {
                                                                name: "isDirectlyAddressable",
                                                                itemId: 'rbtn-device-config-addressable-yes',
                                                                boxLabel: '<b>' + Uni.I18n.translate('general.yes', 'MDC', 'Yes') + '</b>',
                                                                inputValue: true,
                                                                checked: true
                                                            },
                                                            {
                                                                name: "isDirectlyAddressable",
                                                                itemId: 'rbtn-device-config-addressable-no',
                                                                boxLabel: '<b>' + Uni.I18n.translate('general.no', 'MDC', 'No') + '</b>',
                                                                inputValue: false,
                                                                checked: false
                                                            },
                                                            {
                                                                xtype: 'container',
                                                                itemId: 'isDirectlyAddressableError'
                                                            }
                                                        ]
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                fieldLabel: ' ',
                                                padding: '-5 0 15 5',
                                                itemId: 'addressableMessage',
                                                items: [
                                                    {
                                                        xtype: 'container',
                                                        html: '<span style="color: grey;">' + Uni.I18n.translate('deviceconfiguration.directlyAddressable.description', 'MDC', 'If a device is directly addressable, a connection can be made to this device') + '</span>',
                                                        setText: function(text) {
                                                            this.update('<span style="color: grey;">' + text + '</span>');
                                                        }
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                itemId: 'fld-device-config-gateway',
                                                columnWidth: 0.5,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.deviceIsGateway', 'MDC', 'Device is a gateway'),
                                                required: true,
                                                layout: 'fit',
                                                hidden: false,
                                                items: [
                                                    {
                                                        xtype: 'radiogroup',
                                                        itemId: 'deviceIsGatewayCombo',
                                                        columns: 1,
                                                        name: "canBeGateway",
                                                        msgTarget: 'under',
                                                        combineErrors: true,
                                                        vertical: true,
                                                        items: [
                                                            {
                                                                name: "canBeGateway",
                                                                itemId: 'rbtn-device-config-gateway-yes',
                                                                boxLabel: '<b>' + Uni.I18n.translate('general.yes', 'MDC', 'Yes') + '</b>',
                                                                inputValue: true,
                                                                checked: false
                                                            },
                                                            {
                                                                name: "canBeGateway",
                                                                itemId: 'rbtn-device-config-gateway-no',
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
                                                padding: '-5 0 15 5',
                                                itemId: 'gatewayMessage',
                                                items: [
                                                    {
                                                        xtype: 'container',
                                                        html: '<span style="color: grey;">' + Uni.I18n.translate('deviceconfiguration.deviceIsGateway.description', 'MDC', 'Gateways can be used as a connection to slave devices') + '</span>',
                                                        setText: function(text) {
                                                            this.update('<span style="color: grey;">' + text + '</span>');
                                                        }
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                columnWidth: 0.5,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.typeOfGateway', 'MDC', 'Type of gateway'),
                                                itemId: 'typeOfGatewayComboContainer',
                                                padding: '0 0 0 0',
                                                required: true,
                                                layout: 'fit',
                                                hidden: true,
                                                items: [
                                                    {
                                                        xtype: 'radiogroup',
                                                        itemId: 'typeOfGatewayCombo',
                                                        name: "gatewayType",
                                                        msgTarget: 'under',
                                                        combineErrors: true,
                                                        columns: 1,
                                                        vertical: true,
                                                        items: [
                                                            {
                                                                name: "gatewayType",
                                                                itemId: 'rbtn-device-config-gateway-han',
                                                                boxLabel: '<b>' + Uni.I18n.translate('deviceconfiguration.HAN', 'MDC', 'HAN (Home Area Network)') + '</b>',
                                                                afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('deviceconfiguration.HAN.description', 'MDC', 'Connects a small number of slave devices in a home environment') + '</span>',
                                                                padding: '0 0 10 0',
                                                                inputValue: 'HAN',
                                                                checked: true
                                                            },
                                                            {
                                                                name: "gatewayType",
                                                                itemId: 'rbtn-device-config-gateway-lan',
                                                                boxLabel: '<b>' + Uni.I18n.translate('deviceconfiguration.LAN', 'MDC', 'LAN (Local Area Network)') + '</b>',
                                                                afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('deviceconfiguration.LAN.description', 'MDC', 'Connects a large number of slave devices in a local environment') + '</span>',
                                                                padding: '0 0 15 0',
                                                                inputValue: 'LAN',
                                                                checked: false
                                                            }
                                                        ]
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                itemId: 'mdc-device-config-dataLogger-container',
                                                columnWidth: 0.5,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.dataLoggerFunctionality', 'MDC', 'Data logger functionality'),
                                                required: true,
                                                layout: 'fit',
                                                items: [
                                                    {
                                                        xtype: 'radiogroup',
                                                        itemId: 'dataLoggerRadioGroup',
                                                        name: "dataloggerEnabled",
                                                        msgTarget: 'under',
                                                        combineErrors: true,
                                                        columns: 1,
                                                        vertical: true,
                                                        items: [
                                                            {
                                                                name: 'dataloggerEnabled',
                                                                itemId: 'rbtn-device-config-datalogger-yes',
                                                                boxLabel: '<b>' + Uni.I18n.translate('general.yes', 'MDC', 'Yes') + '</b>',
                                                                inputValue: true,
                                                                checked: false
                                                            },
                                                            {
                                                                name: 'dataloggerEnabled',
                                                                itemId: 'rbtn-device-config-datalogger-no',
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
                                                padding: '-5 0 15 5',
                                                itemId: 'dataLoggerMessage',
                                                items: [
                                                    {
                                                        xtype: 'container',
                                                        html: '<span style="color: grey;">' + Uni.I18n.translate('deviceconfiguration.dataLogger.description', 'MDC', 'Can be used as a data logger and share its own channels and registers with its slave devices') + '</span>',
                                                        setText: function(text) {
                                                            this.update('<span style="color: grey;">' + text + '</span>');
                                                        }
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
                                                        text: me.edit
                                                            ? Uni.I18n.translate('general.save', 'MDC', 'Save')
                                                            : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                                        xtype: 'button',
                                                        ui: 'action',
                                                        action: me.edit
                                                            ? 'editDeviceConfiguration'
                                                            : 'createDeviceConfiguration',
                                                        itemId: 'createEditButton'
                                                    },
                                                    {
                                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                                        xtype: 'button',
                                                        ui: 'link',
                                                        itemId: 'cancelLink',
                                                        href: me.returnLink
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
        me.callParent(arguments);
    }
});
