/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationClone', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.util.FormInfoMessage'
    ],
    alias: 'widget.deviceConfigurationClone',
    router: null,
    deviceConfigurationName: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'deviceConfigurationCloneForm',
                ui: 'large',
                title: Uni.I18n.translate('cloneDeviceConfiguration.title',
                    'MDC', "Clone device configuration '{0}'", me.deviceConfigurationName, false),
                items: [
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'uni-form-info-message',
                                text: Uni.I18n.translate('cloneDeviceConfiguration.info', 'MDC',
                                    "The new device configuration is based on the '{0}' and will contain the same data sources, communication features and rule sets. The clone will be inactive.", [me.deviceConfigurationName])
                            }
                        ]
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'deviceConfigurationName',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80,
                        listeners: {
                            afterrender: function (field) {
                                field.focus(false, 200);
                            }
                        },
                        vtype: 'checkForBlacklistCharacters'
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'clone-button',
                                text: Uni.I18n.translate('general.clone', 'MDC', 'Clone'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link',
                                href: me.router.getRoute('administration/devicetypes/view/deviceconfigurations').buildUrl()
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
