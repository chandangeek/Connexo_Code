Ext.define('Mdc.view.setup.deviceconfiguration.ChangeDeviceConfigurationView', {
    extend: 'Uni.view.container.ContentContainer',
    require: [
        'Uni.util.FormEmptyMessage',
        'Uni.util.FormErrorMessage'
    ],
    itemId: 'change-device-configuration-view',
    alias: 'widget.changeDeviceConfigurationView',
    device: null,
    returnLink: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                itemId: 'change-device-configuration-title',
                title: Uni.I18n.translate('device.changeDeviceConfiguration.title', 'MDC', 'Change device configuration'),
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
                                        itemId: 'change-device-configuration-form',
                                        border: false,
                                        width: 800,
                                        layout: {
                                            type: 'vbox'
                                        },
                                        defaults: {
                                            labelWidth: 250
                                        },
                                        items: [
                                            {
                                                xtype: 'uni-form-empty-message',
                                                itemId: 'form-notification',
                                                text: Uni.I18n.translate('device.changeDeviceConfiguration.lossDataNotification', 'MDC', 'The device configuration change can possibly lead to critical data loss (security settings, connection attributes...)'),
                                            },
                                            {
                                                xtype: 'uni-form-error-message',
                                                itemId: 'form-errors',
                                                name: 'form-errors',
                                                width: 707,
                                                hidden: true
                                            },
                                            {
                                                xtype: 'displayfield',
                                                itemId: 'device-configuration-name',
                                                name: 'deviceConfigurationName',
                                                fieldLabel: Uni.I18n.translate('device.changeDeviceConfiguration.currentConfigurationName', 'MDC', 'Current device configuration')
                                            },
                                            {
                                                xtype: 'displayfield',
                                                itemId: 'no-device-configuration',
                                                fieldLabel: Uni.I18n.translate('device.changeDeviceConfiguration.newDeviceConfiguration', 'MDC', 'New device configuration'),
                                                value: Uni.I18n.translate('device.changeDeviceConfiguration.noOtherDeviceConfigurationsDefined', 'MDC', 'No other device configurations defined'),
                                                fieldStyle: 'color: #eb5642',
                                                required: true,
                                                hidden: true
                                            },
                                            {
                                                xtype: 'combo',
                                                itemId: 'new-device-configuration',
                                                name: 'newDeviceConfiguration',
                                                store: 'Mdc.store.DeviceConfigurations',
                                                fieldLabel: Uni.I18n.translate('device.changeDeviceConfiguration.newDeviceConfiguration', 'MDC', 'New device configuration'),
                                                displayField: 'name',
                                                valueField: 'id',
                                                required: true,
                                                allowBlank: false,
                                                editable: false,
                                                queryMode: 'local'
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
                                                        xtype: 'button',
                                                        ui: 'action',
                                                        itemId: 'save-change-device-configuration',
                                                        action: 'save-change-device-configuration',
                                                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                                        disabled: true
                                                    },
                                                    {
                                                        xtype: 'button',
                                                        ui: 'link',
                                                        itemId: 'cancel-change-device-configuration',
                                                        action: 'cancel-change-device-configuration',
                                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
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
