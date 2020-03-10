/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceSendSapNotification', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-send-sap-notification',
    labelWidth: 250,
    width: 600,
    deviceId: null,
    store: 'Mdc.store.DeviceSendSapNotification',

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                layout: 'vbox',
                title: Uni.I18n.translate('deviceSendSapNotification.title', 'MDC', 'Send registered notification to SAP'),
                itemId: 'deviceSendSapNotificationPanel',

                items: [
                    {
                        xtype: 'form',
                        buttonAlign: 'left',
                        layout: {
                            type: 'vbox',
                            align: 'left'
                        },
                        defaults: {
                            labelWidth: me.labelWidth,
                            width: 600
                        },
                        items: [
                            {
                                name: 'errors',
                                ui: 'form-error-framed',
                                itemId: 'deviceSendSapNotificationFormErrors',
                                layout: 'hbox',
                                margin: '0 0 10 0',
                                hidden: true,
                                defaults: {
                                    xtype: 'container',
                                    margin: '0 0 0 10'
                                }
                            },
                            {
                                xtype: 'combobox',
                                editable: false,
                                displayField: 'name',
                                valueField: 'id',
                                queryMode: 'local',
                                fieldLabel: Uni.I18n.translate('sap.webserviceendpoint', 'MDC', 'Web service endpoint'),
                                store: 'Mdc.store.RegisteredNotificationEndpoints',
                                placeholder: Uni.I18n.translate('sap.selectwebserviceendpoint', 'MDC', 'Select a web service endpoint')
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'mdc-deviceAdd-btnContainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.send', 'MDC', 'Send'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'sendSapNotification',
                                        itemId: 'deviceSendSapNotificationBtn'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'deviceCancelSapNotificationBtn',
                                        href: '#/devices/' + me.deviceId
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
        me.callParent(arguments);
    }

});