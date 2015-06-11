Ext.define('Fwc.firmwarecampaigns.controller.Detail', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Fwc.firmwarecampaigns.view.Detail'
    ],

    models: [
        'Fwc.firmwarecampaigns.model.FirmwareCampaign'
    ],

    refs: [
        {
            ref: 'sideMenu',
            selector: 'firmware-campaign-detail firmware-campaign-side-menu'
        }
    ],

    showDetail: function (firmwareCampaignId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            pageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('firmware-campaign-detail', {
                itemId: 'firmware-campaign-detail',
                router: router
            });

        pageView.setLoading();
        me.getModel('Fwc.firmwarecampaigns.model.FirmwareCampaign').load(firmwareCampaignId, {
            success: function (record) {
                me.getApplication().fireEvent('changecontentevent', widget);
                Ext.suspendLayouts();
                me.getSideMenu().down('#firmware-campaign-link').setText(record.get('name'));
                widget.down('firmware-campaigns-detail-form').loadRecord(record);
                widget.down('firmware-campaigns-detail-form property-form').loadRecord(record);
                me.getApplication().fireEvent('loadFirmwareCampaign', record);
                Ext.resumeLayouts(true);
                widget.down('firmware-campaigns-action-menu').record = record;
                widget.down('firmware-campaigns-detail-form #firmware-campaigns-detail-action-menu-button').setDisabled(record.get('status').id !== 'ONGOING');
            },
            callback: function () {
                pageView.setLoading(false);
            }
        });
    }
});