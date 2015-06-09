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
            form = this.getPreview().down('form'),
            store = this.getStore('Fwc.firmwarecampaigns.store.FirmwareCampaigns');

        switch (item.action) {
            case 'cancelCampaign':
                store.getProxy().url = '/api/fwc/campaigns/' + record.id;
                record.set('status', {id: "CANCELLED", localizedValue: "Cancelled"});
                record.save({
                    callback: function (model, operation) {
                        if (operation.success) {
                            form.loadRecord(model);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.campaigns.cancelled', 'FWC', 'Firmware campaign cancelled'));
                        }
                    }
                });
                break;
        }
    }
});