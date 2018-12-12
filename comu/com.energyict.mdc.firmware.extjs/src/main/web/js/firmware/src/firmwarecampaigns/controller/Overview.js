/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.controller.Overview', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Fwc.firmwarecampaigns.view.Overview',
        'Fwc.firmwarecampaigns.view.Add'
    ],

    stores: [
        'Fwc.firmwarecampaigns.store.FirmwareCampaigns',
        'Fwc.store.DeviceTypes',
        'Fwc.firmwarecampaigns.store.FirmwareTypes',
        'Fwc.store.Firmwares',
        'Fwc.store.DeviceGroups'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'firmware-campaigns-detail-form'
        },
        { ref: 'campaignEdit', selector: '#firmware-campaigns-edit' }
    ],
    returnToOverview : false,

    init: function () {
        this.control({
            'firmware-campaigns-overview firmware-campaigns-grid': {
                select: this.showPreview
            },
            '#firmware-campaigns-action-menu': {
                click: this.onActionMenuClicked
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
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        Ext.resumeLayouts(true);
        if (preview.down('firmware-campaigns-action-menu')) {
            preview.down('firmware-campaigns-action-menu').record = record;
            preview.down('#firmware-campaigns-detail-action-menu-button').setVisible(record.get('status').id === 'ONGOING');
        }
    },

    onActionMenuClicked: function (menu, item) {
        switch (item.action) {
            case 'cancelCampaign': this.onCancelCampaign(menu.record);
                break;
            case 'editCampaign':
            case 'editCampaignAndReturnToOverview':
                this.returnToOverview = item.action === 'editCampaignAndReturnToOverview';
                location.href = '#/workspace/firmwarecampaigns/' + encodeURIComponent(menu.record.get('id')) + '/edit';
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
            title: Uni.I18n.translate('firmware.campaigns.cancel.title', 'FWC', "Cancel firmware campaign '{0}'?", record.get('name'))
        });
    },

    doCancelCampaign : function(record) {
        var me = this,
            form = this.getPreview().down('form'),
            store = this.getStore('Fwc.firmwarecampaigns.store.FirmwareCampaigns');

        store.getProxy().url = '/api/fwc/campaigns/' + record.id;
        record.set('status', {id: "CANCELLED", localizedValue: "Cancelled"});
        record.save({
            isNotEdit: true,
            success: function () {
                form.loadRecord(record);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.campaigns.cancelled', 'FWC', 'Firmware campaign cancelled'));
                me.showPreview('', record);
            },
            callback: function () {
                store.getProxy().url = '/api/fwc/campaigns/';
            },
            failure: function () {
                record.reject();
            }
        });
    },

    editCampaign: function(campaignIdAsString) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('firmware-campaigns-add', {
                itemId: 'firmware-campaigns-edit',
                action: 'saveFirmwareCampaign',
                returnLink: me.returnToOverview
                    ? router.getRoute('workspace/firmwarecampaigns/firmwarecampaign').buildUrl({firmwareCampaignId : campaignIdAsString})
                    : router.getRoute('workspace/firmwarecampaigns').buildUrl()
            }),
            dependencies = ['Fwc.store.DeviceTypes'],
            dependenciesCounter = dependencies.length,
            onDependenciesLoaded = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    me.loadModelToEditForm(campaignIdAsString, widget);
                }
            };

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.down('#firmware-campaigns-add-form').setLoading(true);
        widget.down('#btn-add-firmware-campaign').setText(Uni.I18n.translate('general.save', 'FWC', 'Save'));
        Ext.Array.each(dependencies, function (store) {
            me.getStore(store).load(onDependenciesLoaded);
        });
    },

    loadModelToEditForm: function(campaignIdAsString, widget) {
        var me = this,
            editView = me.getCampaignEdit(),
            model = me.getModel('Fwc.firmwarecampaigns.model.FirmwareCampaign'),
            editForm = editView.down('firmware-campaigns-add-form');

        model.load(campaignIdAsString, {
            success: function (campaignRecord) {
                editView.down('firmware-campaigns-add-form').setTitle(
                    Uni.I18n.translate('firmware.campaigns.editFirmwareCampaign', 'FWC', 'Edit firmware campaign')
                );
                me.getApplication().fireEvent('loadFirmwareCampaign', campaignRecord);
                editForm.loadRecordForEdit(campaignRecord);
            }
        });
    }

});