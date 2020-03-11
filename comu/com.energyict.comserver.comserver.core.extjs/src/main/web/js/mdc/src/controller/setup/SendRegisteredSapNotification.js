/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.SendRegisteredSapNotification', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.device.DeviceSendSapNotification'
    ],

    models: [
        'Mdc.model.RegisteredNotificationEndpoints',
        'Mdc.model.DeviceSendSapNotification'
    ],

    stores: [
        'Mdc.store.RegisteredNotificationEndpoints',
        'Mdc.store.DeviceSendSapNotification'
    ],

    init: function () {
        this.control({
            '#deviceSendSapNotificationPanel button[action=sendSapNotification]': {
                click: this.sendSapNotification
            }
        });
    },

    deviceId: null,

    sendRegisteredSapNotification: function(){
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        var sapEndpointData = Ext.create('Mdc.model.DeviceSendSapNotification')
        Ext.create('Mdc.store.RegisteredNotificationEndpoints').load({

            callback: function (records, operation, success) {
                me.deviceId = router.arguments && router.arguments.deviceId ? router.arguments.deviceId : null;
                var widget = Ext.widget('device-send-sap-notification',{
                    deviceId: me.deviceId
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('form').loadRecord(sapEndpointData);
            }
        });
    },

    hasSapCas: function(deviceId){
        var me = this;
        Ext.Ajax.request({
            url: Ext.String.format('api/sap/devices/{deviceId}/hassapcas', deviceId),
            method: 'GET',
            async: false,
            success: function (response) {
                var hasSapCas = Ext.JSON.decode(response.responseText);
                debugger;
            }
        });
    },

    showErrorPanel: function (form) {
        var errorPanel = form.down('#deviceSendSapNotificationFormErrors');
        errorPanel.setVisible(true);
    },

    showErrorAlert: function (){
         var me = this;
         var errorText = me.deviceId + Uni.I18n.translate('general.failedToAddTitle', 'MDC', ' attribute is not set on Device SAP info CAS and/or no LRN is available on current or future data sources on the device. The request sending has failed. See web service history for details');
         var titleText = Uni.I18n.translate('general.failedToAddTitle', 'MDC', 'Couldn\'t perform your action');
         this.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
    },

    sendSapNotification: function(button){
        var me = this;
        var form = button.up('form');

        if (!form.getForm().isValid()) {
            me.showErrorPanel(form);
            return;
        }

        form.updateRecord();
        form.setLoading();
        var record = form.getRecord();
        record.getProxy().setUrl(me.deviceId);
        form.getRecord().save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('sap.webserviceendpoint.hassended', 'MDC', 'Registered notification successfully sent'));
                location.href = "#/devices/" + me.deviceId;
            },
            failure: function (record, operation) {
                me.showErrorPanel(form);
                me.showErrorAlert();
            },
            callback: function(){
                form.setLoading(false);
            }
        });
    }
});

