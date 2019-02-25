/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.controller.Detail', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Tou.view.Detail'
    ],

    models: [
        'Tou.model.TouCampaign'
    ],

    refs: [{
            ref: 'sideMenu',
            selector: 'tou-campaign-detail tou-campaign-side-menu'
        }
    ],

    showDetail: function (touCampaignName) {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        pageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
        widget = Ext.widget('tou-campaign-detail', {
                itemId: 'tou-campaign-detail',
                router: router
            });

        pageView.setLoading();
        me.getModel('Tou.model.TouCampaign').load(touCampaignName, {
            success: function (record) {
                me.getApplication().fireEvent('changecontentevent', widget);
                Ext.suspendLayouts();
                me.getSideMenu().setHeader(record.get('name'));
                widget.down('tou-campaigns-detail-form').loadRecord(record);
                me.getApplication().fireEvent('loadTouCampaign', record);
                Ext.resumeLayouts(true);
                if (widget.down('tou-campaigns-action-menu')) {
                    widget.down('tou-campaigns-action-menu').record = record;
                    widget.down('tou-campaigns-detail-form #tou-campaigns-detail-action-menu-button').setDisabled(record.get('status') !== 'Ongoing');
                }
            },
            callback: function () {
                pageView.setLoading(false);
            }
        });
    }
});