Ext.define('Fwc.firmwarecampaigns.controller.Overview', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Fwc.firmwarecampaigns.view.Overview'
    ],

    stores: [
        'Fwc.firmwarecampaigns.store.FirmwareCampaigns'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'firmware-campaigns-overview firmware-campaigns-detail-form'
        }
    ],

    init: function () {
        this.control({
            'firmware-campaigns-overview firmware-campaigns-grid': {
                select: this.showPreview
            },
            '#firmware-campaigns-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showOverview: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('firmware-campaigns-overview', {
                itemId: 'firmware-campaigns-overview',
                router: router
            });

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview();

        Ext.suspendLayouts();
        preview.loadRecord(record);
        preview.down('property-form').loadRecord(record);
        preview.setTitle(record.get('name'));
        Ext.resumeLayouts(true);
        preview.down('firmware-campaigns-action-menu').record = record;
    },

    chooseAction: function (menu, item) {
        var me = this,
            record = menu.record,
            form = this.getPreview().down('form');
        debugger;
        switch (item.action) {
            case 'cancelCampaign':
                // todo: will be implemented in scope of COMU-62
                record.cancel({
                    success: function () {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.campaigns.cancelled', 'FWC', 'Firmware campaign cancelled'));
                        router.getRoute().forward();
                    },
                    callback: function (model) {
                        form.loadRecord(model);
                    }
                });
                //record.getProxy().setUrl(record.internalId);
                //record.set('status', 'CANCEL');
                //record.save({
                //    callback: function (model) {
                //        form.loadRecord(model);
                //        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.campaigns.cancelled', 'FWC', 'Firmware campaign cancelled'));
                //    }
                //});
                break;
        }
    }
});