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
            selector: 'firmware-campaigns-detail-form'
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
        preview.down('#firmware-campaigns-detail-action-menu-button').setVisible(record.get('status').id === 'ONGOING');
    },

    chooseAction: function (menu, item) {
        switch (item.action) {
            case 'cancelCampaign': this.onCancelCampaign(menu.record);
                break;
        }
    },

    onCancelCampaign : function(record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('firmware.campaigns.cancelCampaign', 'FWC', 'Cancel campaign'),
                confirmation: function () {
                    me.doCancelCampaign(record);
                    this.close();
                }
            });

        confirmationWindow.show({
            msg: Uni.I18n.translate('firmware.campaigns.cancel.msg', 'FWC', 'Cancel the firmware upload for all scheduled devices. Ongoing uploads will not be terminated and successful uploads will not be reversed.'),
            title: Ext.String.format(
                Uni.I18n.translate('firmware.campaigns.cancel.title', 'FWC', "Cancel firmware campaign '{0}'?"),
                record.get('name'))
        });
    },

    doCancelCampaign : function(record) {
        var me = this,
            form = this.getPreview().down('form'),
            store = this.getStore('Fwc.firmwarecampaigns.store.FirmwareCampaigns');

        store.getProxy().url = '/api/fwc/campaigns/' + record.id;
        record.set('status', {id: "CANCELLED", localizedValue: "Cancelled"});
        record.save({
            callback: function (model, operation) {
                store.getProxy().url = '/api/fwc/campaigns/';
                if (operation.success) {
                    form.loadRecord(model);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.campaigns.cancelled', 'FWC', 'Firmware campaign cancelled'));
                }
            }
        });
    }
});