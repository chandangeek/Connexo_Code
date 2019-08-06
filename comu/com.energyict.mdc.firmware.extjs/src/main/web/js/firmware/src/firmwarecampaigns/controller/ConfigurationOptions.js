/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.controller.ConfigurationOptions', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Fwc.firmwarecampaigns.view.ConfigurationOptions'
    ],

    stores: [
        'Fwc.firmwarecampaigns.store.ConfigurationOptions'
    ],

    refs: [
        {
            ref: 'sideMenu',
            selector: 'firmware-campaign-configuration firmware-campaign-side-menu'
        }
    ],


    firmwareCampaignId : null,

    showConfigurationOptions: function (firmwareCampaignId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            pageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            view;

        pageView.setLoading();
        me.getModel('Fwc.firmwarecampaigns.model.FirmwareCampaign').load(firmwareCampaignId, {
            success: function (record) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceType');
                model.load(2, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('changecontentevent', (view = Ext.widget('firmware-campaign-configuration', {
                            itemId: 'firmware-campaign-configuration',
                            router: router,
                            deviceType: deviceType
                        })));
                        me.getSideMenu().setHeader(record.get('name'));
                        me.getApplication().fireEvent('loadFirmwareCampaign', record);
                        var widget = view.down('firmware-specifications'),
                            form = widget ? widget.down('#form') : null;
                        if (form) {
                            form.loadRecord(record.getFirmvareVersionsOptions());
                        }
                        var firmwareStore = Ext.getStore('Fwc.firmwarecampaigns.store.FirmwareVersionsList');
                        firmwareStore.getProxy().setUrl(firmwareCampaignId);
                        var options = {
                            callback: function () {
                                pageView.setLoading(false);
                            }
                        }
                        firmwareStore.load(options);
                    }
                });
            },
            callback: function () {
                pageView.setLoading(false);
            }
        });
    }



});