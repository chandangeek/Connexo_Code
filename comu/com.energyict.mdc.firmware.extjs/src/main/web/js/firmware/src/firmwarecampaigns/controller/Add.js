/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                click: this.addFirmwareCampaign
            },
            'firmware-campaigns-add [action=saveFirmwareCampaign]': {
                click: this.saveFirmwareCampaign
            }
        });
    },

    showAdd: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('firmware-campaigns-add', {
                itemId: 'firmware-campaigns-add',
                action: 'addFirmwareCampaign',
                returnLink: router.getRoute('workspace/firmwarecampaigns').buildUrl()
            }),
            firmwareCampaign = Ext.create('Fwc.firmwarecampaigns.model.FirmwareCampaign'),
            dependencies = ['Fwc.store.DeviceTypes', 'Fwc.store.DeviceGroups'],
            dependenciesCounter = dependencies.length,
            onDependenciesLoaded = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    firmwareCampaign.set('name', 'FW-CP-' + new Date().getTime());
                    widget.down('firmware-campaigns-add-form').loadRecord(firmwareCampaign);
                    widget.setLoading(false);
                }
            };

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();
        Ext.Array.each(dependencies, function (store) {
            me.getStore(store).load(onDependenciesLoaded);
        });
    },

    addFirmwareCampaign: function () {
        var me = this,
            page = me.getPage(),
            form = me.getForm(),
            errorMessage = form.down('uni-form-error-message'),
            baseForm = form.getForm();

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMessage.hide();
        Ext.resumeLayouts(true);
        form.updateRecord();
        page.setLoading();
        form.getRecord().save({
            backUrl: page.returnLink,
            success: function (record, operation) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.campaigns.addSuccess', 'FWC', 'Firmware campaign added'));
                if (page.rendered) {
                    window.location.href = page.returnLink;
                }
            },
            failure: function (record, operation) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (page.rendered && responseText && responseText.errors) {
                    Ext.suspendLayouts();
                    baseForm.markInvalid(responseText.errors);
                    errorMessage.show();
                    Ext.resumeLayouts(true);
                }
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    saveFirmwareCampaign: function () {
        var me = this,
            page = me.getPage(),
            form = me.getForm(),
            nameField = form.down('#firmware-campaign-name'),
            timeBoundaryStartField = form.down('#timeBoundaryStart'),
            timeBoundaryEndField = form.down('#timeBoundaryEnd'),
            errorMessage = form.down('uni-form-error-message'),
            baseForm = form.getForm(),
            nameOrTimeBoundaryChanged = false;

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMessage.hide();
        Ext.resumeLayouts(true);

        if (form.campaignRecordBeingEdited.get('name') != nameField.getValue()) {
            form.campaignRecordBeingEdited.set('name', nameField.getValue());
            nameOrTimeBoundaryChanged = true;
        }
        if (form.campaignRecordBeingEdited.get('timeBoundaryStart') != timeBoundaryStartField.getValue()) {
            form.campaignRecordBeingEdited.set('timeBoundaryStart', timeBoundaryStartField.getValue());
            nameOrTimeBoundaryChanged = true;
        }
        if (form.campaignRecordBeingEdited.get('timeBoundaryEnd') != timeBoundaryEndField.getValue()) {
            form.campaignRecordBeingEdited.set('timeBoundaryEnd', timeBoundaryEndField.getValue());
            nameOrTimeBoundaryChanged = true;
        }

        form.campaignRecordBeingEdited.save({
            backUrl: page.returnLink,
            success: function (record, operation) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('firmware.campaigns.saveSuccess', 'FWC', 'Firmware campaign saved'));
                if (page.rendered) {
                    window.location.href = page.returnLink;
                }
            },
            failure: function (record, operation) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (page.rendered && responseText && responseText.errors) {
                    Ext.suspendLayouts();
                    baseForm.markInvalid(responseText.errors);
                    errorMessage.show();
                    Ext.resumeLayouts(true);
                }
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});