/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.controller.Devices', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Fwc.firmwarecampaigns.view.Devices'
    ],

    stores: [
        'Fwc.firmwarecampaigns.store.Devices'
    ],

    refs: [
        {
            ref: 'sideMenu',
            selector: 'firmware-campaign-devices firmware-campaign-side-menu'
        }
    ],

    init: function () {
        this.control({
            '#firmware-campaigns-device-action-menu': {
                click: this.onActionMenuClicked
            }
        });
    },

    showDevices: function (firmwareCampaignId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            pageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            devicesStore = me.getStore('Fwc.firmwarecampaigns.store.Devices');

        devicesStore.getProxy().setUrl(firmwareCampaignId);
        pageView.setLoading();
        me.getModel('Fwc.firmwarecampaigns.model.FirmwareCampaign').load(firmwareCampaignId, {
            success: function (record) {
                me.getApplication().fireEvent('changecontentevent', Ext.widget('firmware-campaign-devices', {
                    itemId: 'firmware-campaign-devices',
                    router: router,
                    deviceType: record.get('deviceType'),
                    campaignIsOngoing: record.get('status').id === 'ONGOING'
                }));
                me.getSideMenu().down('#firmware-campaign-link').setText(record.get('name'));
                me.getApplication().fireEvent('loadFirmwareCampaign', record);
            },
            callback: function () {
                pageView.setLoading(false);
            }
        });
    },

    onActionMenuClicked: function (menu, item) {
        switch (item.action) {
            case 'cancelDevice':
                this.doCancelDeviceInFirmwareCampaign(menu.record);
                break;
            case 'retryDevice':
                this.doRetryDeviceInFirmwareCampaign(menu.record);
                break;
        }
    },

    doCancelDeviceInFirmwareCampaign: function (record) {
        var me = this,
            url = record.cancelUrl();

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            success: function (response) {
                me.doUpdateRecord(record, response.responseText);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceInFirmwareCampaign.cancelled', 'FWC', 'Firmware upload for device cancelled'));
            }
        });
    },
    doRetryDeviceInFirmwareCampaign: function (record) {
        var me = this,
            url = record.retryUrl();

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            success: function (response) {
                me.doUpdateRecord(record, response.responseText);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceInFirmwareCampaign.retry', 'FWC', 'Firmware upload for device rescheduled'));
            }
        });
    },
    doUpdateRecord: function(record, responseText) {
        var result = Ext.JSON.decode(responseText);
        if (result) {
            var status = result['status'],
                startedOn = result['startedOn'],
                finishedOn = result['finishedOn'];
            record.beginEdit();
            record.set('status', status);
            record.set('startedOn', startedOn);
            record.set('finishedOn', finishedOn);
            record.endEdit();
        }

    }



});