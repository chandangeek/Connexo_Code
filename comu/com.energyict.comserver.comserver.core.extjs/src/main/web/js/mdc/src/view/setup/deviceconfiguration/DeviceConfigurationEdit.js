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
                                                },
                                                vtype: 'checkForBlacklistCharacters'
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
                                                xtype: 'checkboxfield',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.isDirectlyAddressable', 'MDC', 'Directly addressable'),
                                                itemId: 'mdc-device-config-directly-addressable-checkbox',
                                                name: 'isDirectlyAddressable',
                                                boxLabel: Uni.I18n.translate('deviceconfiguration.directlyAddressable.boxLabel', 'MDC', 'Connection can be made to this device'),
                                                checked: false
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                fieldLabel: ' ',
                                                padding: '-5 0 15 5',
                                                itemId: 'mdc-device-config-directly-addressable-msg',
                                                items: [
                                                    {
                                                        xtype: 'container',
                                                        html: '<span style="color: grey;">' + Uni.I18n.translate('deviceconfiguration.directlyAddressableMessage', 'MDC', "The protocol doesn't support directly addressing the device.") + '</span>'
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'checkboxfield',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.deviceIsGateway', 'MDC', 'Device is a gateway'),
                                                itemId: 'mdc-device-config-gateway-checkbox',
                                                name: 'canBeGateway',
                                                boxLabel: Uni.I18n.translate('deviceconfiguration.deviceIsGateway.boxLabel', 'MDC', 'Can be used as a connection to slave devices'),
                                                checked: false
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                fieldLabel: ' ',
                                                padding: '-5 0 15 5',
                                                itemId: 'mdc-device-config-gateway-msg',
                                                items: [
                                                    {
                                                        xtype: 'container',
                                                        html: '<span style="color: grey;">' + Uni.I18n.translate('deviceconfiguration.gatewayMessage', 'MDC', "The protocol doesn't support the device to function as a gateway.") + '</span>'
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
                                                xtype: 'checkboxfield',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.dataLoggerFunctionality', 'MDC', 'Data logger functionality'),
                                                itemId: 'mdc-device-config-dataLogger-checkbox',
                                                name: 'dataloggerEnabled',
                                                boxLabel: Uni.I18n.translate('deviceconfiguration.dataLogger.boxLabel', 'MDC', 'Can be used as a data logger and share its own channels and registers with its slave devices'),
                                                checked: false
                                            },
                                            {
                                                xtype: 'checkboxfield',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.multiElementFunctionality', 'MDC', 'Multi-element functionality'),
                                                itemId: 'mdc-device-config-multiElement-checkbox',
                                                name: 'multiElementEnabled',
                                                boxLabel: Uni.I18n.translate('deviceconfiguration.multielement.boxLabel', 'MDC', 'Can be used as a multi-element device and contain logical devices'),
                                                checked: false
                                            },
                                            {
                                                xtype: 'checkboxfield',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.validateOnStore', 'MDC', 'Validate data on storage'),
                                                itemId: 'mdc-device-config-validateOnStore-checkbox',
                                                name: 'validateOnStore',
                                                boxLabel: Uni.I18n.translate('deviceconfiguration.validateOnStore.boxLabel', 'MDC', 'Data will be validated when it\'s available in the system'),
                                                checked: true
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
