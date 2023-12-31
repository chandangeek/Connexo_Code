/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.controller.Add', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Tou.store.DaysWeeksMonths'
    ],

    views: [
        'Tou.view.Add'
    ],

    models: [
        'Tou.model.DayWeekMonth'
    ],

    stores: [
        'Tou.store.DeviceTypes',
        'Tou.store.DeviceGroups',
        'Tou.store.DaysWeeksMonths',
        'Tou.store.AllowedCalendars',
        'Tou.store.AllowedDeviceTypeOptions',
        'Tou.store.ComTasks',
        'Tou.store.ConnectionStrategy'
    ],

    refs: [{
            ref: 'page',
            selector: 'tou-campaigns-add'
        }, {
            ref: 'form',
            selector: 'tou-campaigns-add tou-campaigns-add-form'
        }
    ],

    init: function () {
        this.control({
            'tou-campaigns-add [action=addTouCampaign]': {
                click: this.addTouCampaign
            },
            'tou-campaigns-add [action=saveTouCampaign]': {
                click: this.saveTouCampaign
            }
        });
    },

    showAdd: function () {
        var me = this,
        router = me.getController('Uni.controller.history.Router'),
        widget = Ext.widget('tou-campaigns-add', {
                itemId: 'tou-campaigns-add',
                action: 'addTouCampaign',
                returnLink: router.getRoute('workspace/toucampaigns').buildUrl()
            }),
        touCampaign = Ext.create('Tou.model.TouCampaign'),
        dependencies = ['Tou.store.DeviceTypes', 'Tou.store.DeviceGroups'],
        dependenciesCounter = dependencies.length,
        onDependenciesLoaded = function () {
            dependenciesCounter--;
            if (!dependenciesCounter) {
                touCampaign.set('name', 'TOU-CP-' + new Date().getTime());
                widget.down('tou-campaigns-add-form').loadRecord(touCampaign);
                widget.setLoading(false);
            }
        };

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();
        Ext.Array.each(dependencies, function (store) {
            me.getStore(store).load(onDependenciesLoaded);
        });
    },
    processRecord: function (form, record, justUpdated) {
        var deviceTypeId = record.get('deviceType');
        var activationStartItem = form.down('#activationStart');
        var activationEndItem = form.down('#activationEnd');
        if (activationStartItem) {
            var activationStartTime = activationStartItem.getValue();
            activationStartTime += new Date().getTimezoneOffset() * 60;
            record.set('activationStart', activationStartTime);
        }
        if (activationEndItem) {
            var activationEndTime = activationEndItem.getValue();
            activationEndTime += new Date().getTimezoneOffset() * 60;
            record.set('activationEnd', activationEndTime);
        }
        if (!justUpdated) {
            if (deviceTypeId)
                record.set('deviceType', {
                    "id": deviceTypeId
                });
            var activateCalendarItem = form.down('#activate-calendar');
            if (activateCalendarItem) {
                var activationOption,
                activationDate;
                activationOption = activateCalendarItem.getOptionValue();
                if (activationOption) record.set('activationOption', activationOption);
                if (activationOption == 'onDate' && activateCalendarItem.getDateValue())
                    record.set('activationDate', activateCalendarItem.getDateValue());
                else if (record.data && record.data['activationDate'] !== undefined)
                    delete record.data["activationDate"];
            }
            var allowedCalendarItem = form.down('#tou-campaign-allowed-calendar');
            if (allowedCalendarItem) {
                record.set('calendar', {
                    "id": allowedCalendarItem.getValue()
                });
            }
            var timeValidationPeriodItem = form.down('#period-combo');
            var timeValidationValueItem = form.down('#period-number');
            if (timeValidationPeriodItem && timeValidationValueItem) {
                var timeInSec = timeValidationValueItem.getValue();
                var timeValidationPeriod = timeValidationPeriodItem.findRecordByDisplay(timeValidationPeriodItem.getRawValue()).get('name');
                switch (timeValidationPeriod) {
                case "weeks":
                    timeInSec *= 7;
                case "days":
                    timeInSec *= 24;
                case "hours":
                    timeInSec *= 60;
                case "minutes":
                    timeInSec *= 60;
                    break;
                default:
                    break;
                }

                if (activateCalendarItem && (
                    activateCalendarItem.getOptionValue() === "immediately"
                 || activateCalendarItem.getOptionValue() === "onDate"
                )) {
                    record.set('validationTimeout', timeInSec);
                } else {
                    record.set('validationTimeout', null);
                }
            }
        }

        var sendCalendarComTaskField = form.down('[name=sendCalendarComTask]');
        var sendCalendarComTask = sendCalendarComTaskField.store.getById(sendCalendarComTaskField.value);

        if (sendCalendarComTask) {
            record.set('sendCalendarComTask', sendCalendarComTask.getData());
        }

        var sendCalendarConnectionStrategyField = form.down('[name=sendCalendarConnectionStrategy]');
        var sendCalendarConnectionStrategy = sendCalendarConnectionStrategyField.store.getById(
            sendCalendarConnectionStrategyField.value
        );

        if (sendCalendarConnectionStrategy) {
            record.set('sendCalendarConnectionStrategy', sendCalendarConnectionStrategy.getData());
        }

        var activateCalendarItem = form.down('#activate-calendar');
        if (activateCalendarItem.getOptionValue() === "immediately"
        || activateCalendarItem.getOptionValue() === "onDate") {
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

        if (record && record.data && record.data["devices"] !== undefined)
            delete record.data["devices"];
        if (record && record.data && record.data["timeBoundary"] !== undefined)
            delete record.data["timeBoundary"];
        if (record && record.data && record.data["startedOn"] !== undefined)
            delete record.data["startedOn"];
        if (record && record.data && record.data["finishedOn"] !== undefined)
            delete record.data["finishedOn"];
        if (record && record.data && record.data["status"] !== undefined)
            delete record.data["status"];
    },

    addTouCampaign: function () {
        var me = this,
        page = me.getPage(),
        form = me.getForm(),
        errorMessage = form.down('uni-form-error-message'),
        periodCombo = form.down('#period-combo'),
        periodCount = form.down('#period-number'),
        baseForm = form.getForm(),
        activateCalendarItem = form.down('#activate-calendar');

        if (!form.isValid() || !activateCalendarItem.getOptionValue()) {
            var activateCalendarError = form.down('#activateCalendarErrorMain');
            activateCalendarItem.getOptionValue() && activateCalendarError ? activateCalendarError.hide() : activateCalendarError.show();
            errorMessage.show();
            return;
        }
        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMessage.hide();
        Ext.resumeLayouts(true);
        form.updateRecord();
        page.setLoading();
        var record = form.getRecord()
            me.processRecord(form, form.getRecord());
        record.save({
            backUrl: page.returnLink,
            success: function (record, operation) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('tou.campaigns.addSuccess', 'TOU', 'ToU calendar campaign added'));
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

    saveTouCampaign: function () {
        var me = this,
        page = me.getPage(),
        form = me.getForm(),
        nameField = form.down('#tou-campaign-name'),
        timeBoundaryStartField = form.down('#activationStart'),
        timeBoundaryEndField = form.down('#activationEnd'),
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

        var touCampaignId = form.campaignRecordBeingEdited.get('id');
        var touCampaignNewName = nameField.getValue();

        if (form.campaignRecordBeingEdited.get('name') != nameField.getValue()) {
            form.campaignRecordBeingEdited.set('name', nameField.getValue());
            nameOrTimeBoundaryChanged = true;
        }
        if (form.campaignRecordBeingEdited.get('activationStart') != timeBoundaryStartField.getValue()) {
            form.campaignRecordBeingEdited.set('activationStart', timeBoundaryStartField.getValue());
            nameOrTimeBoundaryChanged = true;
        }
        if (form.campaignRecordBeingEdited.get('activationEnd') != timeBoundaryEndField.getValue()) {
            form.campaignRecordBeingEdited.set('activationEnd', timeBoundaryEndField.getValue());
            nameOrTimeBoundaryChanged = true;
        }
        me.processRecord(form, form.campaignRecordBeingEdited, true);
        var url = form.campaignRecordBeingEdited.getProxy().setUpdateUrl(touCampaignId);
        //page.returnLink = page.returnLink.replace(touCampaignOldName, touCampaignNewName);
        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            jsonData: form.campaignRecordBeingEdited.data, // can be any object or JSON string
            success: function (response, opts) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('tou.campaigns.saveSuccess', 'TOU', 'ToU calendar campaign saved'));
                if (page.rendered) {
                    window.location.href = page.returnLink;
                }
            },
            failure: function (record, operation) {
                var responseText = Ext.decode(record.responseText, true);

                if (page.rendered && responseText && responseText.errors) {
                    Ext.suspendLayouts();
                    baseForm.markInvalid(responseText.errors);
                    errorMessage.show();
                    Ext.resumeLayouts(true);
                }
            },
            callback: function () {
                page.setLoading(false);
                form.campaignRecordBeingEdited.getProxy().resetUpdateUrl();
            }
        });
    }
});