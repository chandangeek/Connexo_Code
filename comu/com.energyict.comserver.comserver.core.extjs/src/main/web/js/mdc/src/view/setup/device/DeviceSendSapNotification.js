/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceSendSapNotification', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-send-sap-notification',
    labelWidth: 250,
    width: 600,
    deviceId: null,
    endPointsStore: null,

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
                                xtype: 'uni-form-error-message',
                                itemId: 'deviceSendSapNotificationFormErrors',
                                text: Uni.I18n.translate('general.formErrors', 'MDC', 'There are errors on this page that require your attention.'),
                                hidden: true
                            },
                            {
                                xtype: 'fieldcontainer',
                                layout: 'vbox',
                                fieldLabel: Uni.I18n.translate('sap.webserviceendpoint', 'MDC', 'Web service endpoint'),
                                items: [
                                    {
                                        xtype: 'component',
                                        itemId: 'deviceSendSapNotificationEndpointNoItem',
                                        html: 'No active web service endpoints available',
                                        hidden: me.endPointsStore.getCount(),
                                        style: {
                                             'margin-top': '4px',
                                             'color': 'red'
                                        }
                                    },
                                    {
                                        xtype: 'combobox',
                                        editable: false,
                                        displayField: 'name',
                                        valueField: 'id',
                                        queryMode: 'local',
                                        itemId: 'deviceSendSapNotificationEndpointCombo',
                                        store: me.endPointsStore,
                                        required: true,
                                        allowBlank: false,
                                        hidden: !me.endPointsStore.getCount(),
                                        html: 'No active web service endpoints available',
                                        name: 'id',
                                        fieldLabel: Uni.I18n.translate('sap.webserviceendpoint', 'MDC', 'Web service endpoint'),
                                    },
                                ]
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