/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.controller.Devices', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Tou.view.Devices'
    ],

    stores: [
        'Tou.store.Devices'
    ],

    refs: [
        {
            ref: 'sideMenu',
            selector: 'tou-campaign-devices tou-campaign-side-menu'
        }
    ],

    init: function () {
        this.control({
            '#tou-campaigns-device-action-menu': {
                click: this.onActionMenuClicked
            }
        });
    },

    showDevices: function (touCampaignName) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            pageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            devicesStore = me.getStore('Tou.store.Devices');

        devicesStore.getProxy().setUrl(touCampaignName);
        pageView.setLoading();
        me.getModel('Tou.model.TouCampaign').load(touCampaignName, {
            success: function (record) {
                me.getApplication().fireEvent('changecontentevent', Ext.widget('tou-campaign-devices', {
                    itemId: 'tou-campaign-devices',
                    router: router,
                    deviceType: record.get('deviceType'),
                    campaignIsOngoing:record.get('status') === 'Ongoing',
                    returnLink: router.getRoute('workspace/toucampaigns/toucampaign/devices').buildUrl()
                }));
                me.getSideMenu().setHeader(record.get('name'));
                me.getApplication().fireEvent('loadTouCampaign', record);
            },
            callback: function () {
                pageView.setLoading(false);
            }
        });
    },

    onActionMenuClicked: function (menu, item) {
        switch (item.action) {
            case 'cancelDevice':
                this.doCancelDeviceInTouCampaign(menu.record);
                break;
            case 'retryDevice':
                this.doRetryDeviceInTouCampaign(menu.record);
                break;
        }
    },

    doCancelDeviceInTouCampaign: function (record) {
        var me = this,
            pageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            url = record.cancelUrl(),
            devicesWidget = pageView.down("#tou-campaign-devices");

        Ext.Ajax.request({
            url: url,
            jsonData : {"id" : record.get('device').id},
            method: 'PUT',
            success: function (response) {
                //me.doUpdateRecord(record, response.responseText);
                me.getApplication().fireEvent('acknowledge', ' Time of use upload for device cancelled');
                if (pageView.rendered) {
                   window.location.href = devicesWidget.returnLink;
                }
            }
        });
    },
    doRetryDeviceInTouCampaign : function (record) {
        var me = this,
            pageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            url = record.retryUrl(),
            devicesWidget = pageView.down("#tou-campaign-devices");

        Ext.Ajax.request({
            url: url,
            jsonData : {"id" : record.get('device').id},
            method: 'PUT',
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', ' Time of use upload for device rescheduled');
                if (pageView.rendered) {
                    window.location.href = devicesWidget.returnLink;
                }
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