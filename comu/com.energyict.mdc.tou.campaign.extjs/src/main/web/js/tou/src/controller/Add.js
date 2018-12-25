/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
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
        'Fwc.store.DeviceGroups',
        'Tou.store.DaysWeeksMonths',
        'Tou.store.AllowedCalendars'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'tou-campaigns-add'
        },
        {
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
            dependencies = ['Tou.store.DeviceTypes', 'Fwc.store.DeviceGroups'],
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

    addTouCampaign: function () {
        var me = this,
            page = me.getPage(),
            form = me.getForm(),
            errorMessage = form.down('uni-form-error-message'),
            periodCombo = form.down('#period-combo'),
            periodCount = form.down('#period-number'),
            baseForm = form.getForm();

        if (!form.isValid()) {
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
        var activateCalendarItem = form.down('#activate-calendar');
        if (activateCalendarItem){
            record.set('activationDate', activateCalendarItem.getValue());
        }
        var allowedCalendarItem = form.down('#tou-campaign-allowed-calendar');
        if (allowedCalendarItem){
              record.set('calendar', allowedCalendarItem.getValue());
        }
        var timeValidationPeriodItem = form.down('#period-combo');
        var timeValidationValueItem = form.down('#period-number');
        if (timeValidationPeriodItem && timeValidationValueItem){
            var timeInSec = timeValidationValueItem.getValue();
            var timeValidationPeriod = timeValidationPeriodItem.getValue();
            switch(timeValidationPeriod){
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
            if (activateCalendarItem && activateCalendarItem.getValue() == "Immediately"){
               record.set('timeValidation', timeInSec);
             }else{
               record.set('timeValidation', null);
             }
        }

        record.save({
            backUrl: page.returnLink,
            success: function (record, operation) {
                me.getApplication().fireEvent('acknowledge', 'Tou campaign added');
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
                me.getApplication().fireEvent('acknowledge', 'Tou campaign saved');
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