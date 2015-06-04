Ext.define('Fwc.firmwarecampaigns.controller.Add', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Fwc.firmwarecampaigns.view.Add'
    ],

    models: [
        'Fwc.model.FirmwareManagementOptions',
        'Fwc.firmwarecampaigns.model.FirmwareManagementOption',
        'Fwc.firmwarecampaigns.model.FirmwareCampaign'
    ],

    stores: [
        'Fwc.store.DeviceTypes',
        'Fwc.firmwarecampaigns.store.FirmwareTypes',
        'Fwc.store.Firmwares',
        'Fwc.store.DeviceGroups'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'firmware-campaigns-add'
        },
        {
            ref: 'form',
            selector: 'firmware-campaigns-add firmware-campaigns-add-form'
        }
    ],

    init: function () {
        this.control({
            'firmware-campaigns-add [action=addFirmwareCampaign]': {
                click: this.saveFirmwareCampaign
            }
        });
    },

    showAdd: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('firmware-campaigns-add', {
                itemId: 'firmware-campaigns-add',
                returnLink: router.getRoute('workspace/firmwarecampaigns').buildUrl()
            }),
            firmwareCampaign = Ext.create('Fwc.firmwarecampaigns.model.FirmwareCampaign'),
            dependences = ['Fwc.store.DeviceTypes',
                'Fwc.store.DeviceGroups'],
            dependencesCounter = dependences.length,
            dependencesOnLoad = function () {
                dependencesCounter--;
                if (!dependencesCounter) {
                    firmwareCampaign.set('name', 'FW-CP-' + new Date().getTime());
                    widget.down('firmware-campaigns-add-form').loadRecord(firmwareCampaign);
                    widget.setLoading(false);
                }
            };

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();
        Ext.Array.each(dependences, function (store) {
            me.getStore(store).load(dependencesOnLoad);
        });
    },

    saveFirmwareCampaign: function () {
        var me = this,
            page = me.getPage(),
            form = me.getForm(),
            errorMessage = form.down('uni-form-error-message'),
            baseForm= form.getForm();

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMessage.hide();
        Ext.resumeLayouts(true);
        form.updateRecord();
        page.setLoading();
        form.getRecord().save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);

                page.setLoading(false);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', operation.action === 'create'
                        ? Uni.I18n.translate('firmware.campaigns.addSuccess', 'FWC', 'Firmware campaign added')
                        : Uni.I18n.translate('firmware.campaigns.saveSuccess', 'FWC', 'Firmware campaign saved'));
                    if (page.rendered) {
                        window.location.href = page.returnLink;
                    }
                } else {
                    if (page.rendered && responseText && responseText.errors) {
                        Ext.suspendLayouts();
                        baseForm.markInvalid(responseText.errors);
                        errorMessage.show();
                        Ext.resumeLayouts(true);
                    }
                }
            }
        });
    }
});