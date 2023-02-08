/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.controller.Add', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Fwc.firmwarecampaigns.store.DaysWeeksMonths'
    ],

    views: [
        'Fwc.firmwarecampaigns.view.Add'
    ],

    models: [
        'Fwc.model.FirmwareManagementOptions',
        'Fwc.firmwarecampaigns.model.FirmwareManagementOption',
        'Fwc.firmwarecampaigns.model.FirmwareCampaign',
        'Fwc.firmwarecampaigns.model.DayWeekMonth'
    ],

    stores: [
        'Fwc.store.DeviceTypes',
        'Fwc.firmwarecampaigns.store.FirmwareTypes',
        'Fwc.store.Firmwares',
        'Fwc.store.DeviceGroups',
        'Fwc.firmwarecampaigns.store.DaysWeeksMonths',
        'Fwc.firmwarecampaigns.store.ComTasksForValidate',
        'Fwc.firmwarecampaigns.store.FWComTask',
        'Fwc.firmwarecampaigns.store.ConnectionStrategy'
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

                    if (me.getStore('Fwc.store.DeviceTypes').getCount() === 0) {
                        widget.down('#firmware-campaign-device-type').hide();
                        widget.down('#firmware-campaign-device-type').allowBlank = true;
                        widget.down('#no-device-type').show();
                    }
                    if (me.getStore('Fwc.store.DeviceGroups').getCount() === 0) {
                        widget.down('#firmware-campaign-device-group').hide();
                        widget.down('#device-group-info').hide();
                        widget.down('#firmware-campaign-device-group').allowBlank = true;
                        widget.down('#no-device-group').show();
                    }

                    widget.setLoading(false);
                }
            };

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();
        Ext.Array.each(dependencies, function (store) {
            me.getStore(store).load(onDependenciesLoaded);
        });
    },

    convertTimeFormat: function (timeInSec) {
        return timeInSec * 1000 + new Date().getTimezoneOffset() * 60000;
    },

    addFirmwareCampaign: function () {
        var me = this,
            page = me.getPage(),
            form = me.getForm(),
            errorMessage = form.down('uni-form-error-message'),
            periodCombo = form.down('#period-combo'),
            periodCount = form.down('#period-number'),
            timeBoundaryStart = form.down('#timeBoundaryStart'),
            timeBoundaryEnd = form.down('#timeBoundaryEnd'),
            baseForm = form.getForm(),
            firmwareVersionsView = form.down('firmware-version-options');

        var versionOptions = firmwareVersionsView.getDataFromChecks(true);

        if (!form.isValid() || !versionOptions) {
            errorMessage.show();
            return;
        }
        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMessage.hide();
        Ext.resumeLayouts(true);
        form.updateRecord();
        page.setLoading();
        var record = form.getRecord();
        var propertyForm = form.down('property-form');
        var firmwareVersionsView = form.down('firmware-version-options');


        if (record.get('managementOption')) {
            record.set('validationTimeout', {
                count: periodCount.getValue(),
                timeUnit: periodCombo.findRecordByDisplay(periodCombo.getRawValue()).get('name')
            });
        }

        if (versionOptions) {
            record.set('checkOptions', versionOptions);
        }

        record.set('timeBoundaryStart', me.convertTimeFormat(timeBoundaryStart.getValue()));
        record.set('timeBoundaryEnd', me.convertTimeFormat(timeBoundaryEnd.getValue()));

        var sendCalendarComTaskField = form.down('[name=firmwareUploadComTask]');
        var firmwareUploadComTask = sendCalendarComTaskField.store.getById(sendCalendarComTaskField.value);

        if (firmwareUploadComTask) {
            record.set('firmwareUploadComTask', firmwareUploadComTask.getData());
        }

        var sendFirmwareConnectionStrategyField = form.down('[name=firmwareUploadConnectionStrategy]');
        var firmwareUploadConnectionStrategy = sendFirmwareConnectionStrategyField.store.getById(
            sendFirmwareConnectionStrategyField.value
        );

        if (firmwareUploadConnectionStrategy) {
            record.set('firmwareUploadConnectionStrategy', firmwareUploadConnectionStrategy.getData());
        }

        if (record.get('managementOption') && (record.get('managementOption').id === "activate"
                || record.get('managementOption').id === "activateOnDate")) {
            var validationComTaskField = form.down('[name=validationComTask]');
            var validationComTask = validationComTaskField.store.getById(
                validationComTaskField.value
            );

            if (validationComTask) {
                record.set('validationComTask', validationComTask.getData());
            }

            var validationConnectionStrategyField = form.down('[name=validationConnectionStrategy]');
            var validationConnectionStrategy = validationConnectionStrategyField.store.getById(
                validationConnectionStrategyField.value
            );

            if (validationConnectionStrategy) {
                record.set('validationConnectionStrategy', validationConnectionStrategy.getData());
            }
        } else {
            record.set('validationComTask', undefined);
            record.set('validationConnectionStrategy', undefined);
        }

        record.save({
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

        if (!form.isValid()) {
            errorMessage.show();
            return;
        }
        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMessage.hide();
        Ext.resumeLayouts(true);

        if (form.campaignRecordBeingEdited.get('name') != nameField.getValue()) {
            form.campaignRecordBeingEdited.set('name', nameField.getValue());
            nameOrTimeBoundaryChanged = true;
        }
        if (form.campaignRecordBeingEdited.get('timeBoundaryStart') != me.convertTimeFormat(timeBoundaryStartField.getValue())) {
            form.campaignRecordBeingEdited.set('timeBoundaryStart', me.convertTimeFormat(timeBoundaryStartField.getValue()));
            nameOrTimeBoundaryChanged = true;
        }
        if (form.campaignRecordBeingEdited.get('timeBoundaryEnd') != me.convertTimeFormat(timeBoundaryEndField.getValue())) {
            form.campaignRecordBeingEdited.set('timeBoundaryEnd', me.convertTimeFormat(timeBoundaryEndField.getValue()));
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
