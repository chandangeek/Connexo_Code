/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.OfflineComServerEdit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.TimeUnitsWithoutMilliseconds'
    ],

    models: [
        'Mdc.model.ComServer',
        'Mdc.model.OutboundComPort',
        'Mdc.model.InboundComPort',
        'Mdc.model.ModemInitString'
    ],

    views: [
        'setup.comserver.OfflineComServerEdit'
    ],

    stores: [
        'ComServers',
        'Mdc.store.LogLevels',
        'Mdc.store.TimeUnitsWithoutMilliseconds'
    ],

    refs: [
        {
            ref: 'offlineComServerEditPage',
            selector: 'offlineComServerEdit'
        }
    ],

    init: function () {
        this.control({
            'offlineComServerEdit button[action=saveModel]': {
                click: this.saveComServer
            }
        });
    },

    showEditView: function (id) {
        var me = this,
            widget = Ext.widget('offlineComServerEdit'),
            model = me.getModel('Mdc.model.ComServer'),
            comServersStore = this.getStore('Mdc.store.ComServers'),
            logLevelsStore = this.getStore('Mdc.store.LogLevels');

        comServersStore.load();
        logLevelsStore.load();
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setEdit(true, '#/administration/offlinecomservers');

        widget.setLoading(true);

        model.load(id, {
            success: function (record) {
                var comServerType = record.get('comServerType'),
                    form = widget.down('form'),
                    title;

                me.comServerModel = record;

                me.getApplication().fireEvent('comServerOverviewLoad', record);

                switch (comServerType) {
                    case 'Offline':
                        title = Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [Ext.String.htmlEncode(record.get('name'))]);
                        break;
                }

                form.setTitle(title);
                form.down('[name=comServerTypeVisual]').setValue(comServerType);
                form.down('[name=comServerTypeVisual]').show();
                me.modelToForm(record, form);
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
    },

    showOfflineAddView: function () {
        var me = this,
            widget = Ext.widget('offlineComServerEdit'),
            model = Ext.create(Mdc.model.ComServer),
            form;

        me.getApplication().fireEvent('changecontentevent', widget);
        form = widget.down('form');
        form.setTitle(Uni.I18n.translate('comServer.addOffline', 'MDC', 'Add mobile communication server'));
        widget.setEdit(false, '#/administration/offlinecomservers');

        model.beginEdit();
        model.set('comServerType', 'Offline');
        model.set('serverLogLevel', 'Warning');
        model.set('communicationLogLevel', 'Warning');
        model.set('changesInterPollDelay', {count: '5', timeUnit: 'minutes'});
        model.set('schedulingInterPollDelay', {count: '60', timeUnit: 'seconds'});
        model.endEdit();
        me.comServerModel = model;
        me.modelToForm(model, form);
    },

    saveComServer: function (button) {
        var me = this,
            page = me.getOfflineComServerEditPage(),
            form = page.down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            model;

        try {
            model = me.formToModel();
            button.setDisabled(true);
            page.setLoading('Saving...');
            formErrorsPanel.hide();
            form.getForm().clearInvalid();


            model.setChangesInterPollDelay(Ext.create('Mdc.model.field.TimeInfo', {
                count: model.get('changesInterPollDelay').count,
                timeUnit: model.get('changesInterPollDelay').timeUnit
            }));
            model.setSchedulingInterPollDelay(Ext.create('Mdc.model.field.TimeInfo', {
                    count: model.get('schedulingInterPollDelay').count,
                    timeUnit: model.get('schedulingInterPollDelay').timeUnit
                })
            );


            model.save({
                backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/offlinecomservers').buildUrl(),
                callback: function (model, operation, success) {
                    page.setLoading(false);
                    button.setDisabled(false);

                    if (success) {
                        me.onSuccessSaving(operation.action, model.get('comServerType'));
                    } else {
                        var json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {

                            var errorsToShow = [];

                            Ext.each(json.errors, function (item) {
                                switch (item.id) {
                                    case 'schedulingInterPollDelay':
                                        item.id ='schedulingInterPollDelay[count]';
                                        item.msg = Uni.I18n.translate('comServer.formFieldErr.minimalAcceptableValue60sec', 'MDC', 'Minimal acceptable value is 60 seconds');
                                        errorsToShow.push(item);
                                        break;
                                    case 'changesInterPollDelay':
                                        item.id='changesInterPollDelay[count]';
                                        item.msg = Uni.I18n.translate('comServer.formFieldErr.minimalAcceptableValue60sec', 'MDC', 'Minimal acceptable value is 60 seconds');
                                        errorsToShow.push(item);
                                        break;
                                    default:
                                        errorsToShow.push(item);

                                }
                            });

                            form.getForm().markInvalid(errorsToShow);
                            formErrorsPanel.show();
                        }

                    }
                }
            });
        }catch(err){
            formErrorsPanel.show();
        }
    },

    modelToForm: function (model, form) {
        var me = this,
            data = model.getData(),
            basicForm = form.getForm(),
            comServersStore = this.getStore('Mdc.store.ComServers'),
            logLevelsStore = me.getStore('Mdc.store.LogLevels'),
            timeUnitsStore = me.getStore('Mdc.store.TimeUnitsWithoutMilliseconds'),
            values = {};

        Ext.Object.each(data, function (key, value) {
            if (Ext.isObject(value)) {
                Ext.Object.each(value, function (valKey, valValue) {
                    values[key + '[' + valKey + ']'] = valValue;
                });
            } else {
                values[key] = value;
            }
        });

        comServersStore.load();
        logLevelsStore.load(function () {
            timeUnitsStore.load(function () {
                basicForm.setValues(values);
            });
        });
    },

    formToModel: function () {
        var form = this.getOfflineComServerEditPage().down('form'),
            queryString = Ext.Object.toQueryString(form.getValues()),
            values = Ext.Object.fromQueryString(queryString, true),
            model = this.comServerModel;
        if (form.isValid()){
            model.beginEdit();
            model.set(values);
            model.endEdit();
        }else{
            throw Uni.I18n.translate('comServer.form.invalid', 'MDC', 'Form contains invalid values.')
        }
        return model;
    },

    onSuccessSaving: function (action, comServerType) {
        var router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (action) {
            case 'create':
                switch (comServerType) {
                    case 'Offline':
                        messageText = Uni.I18n.translate('offline.comServer.acknowledge.createSuccess', 'MDC', 'Mobile communication server added');
                        break;
                }
                break;
            case 'update':
                switch (comServerType) {
                    case 'Offline':
                        messageText = Uni.I18n.translate('offline.comServer.acknowledge.updateSuccess', 'MDC', 'Mobile communication server saved');
                        break;
                }
                break;
        }
        this.getApplication().fireEvent('acknowledge', messageText);
        router.getRoute('administration/offlinecomservers').forward();
    },

    onFailureSaving: function (response) {
        var form = this.getOfflineComServerEditPage().down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            basicForm = form.getForm(),
            responseText;

        if (response.status == 400) {
            responseText = Ext.decode(response.responseText, true);
            if (responseText && responseText.errors) {
                Ext.Array.each(responseText.errors, function (item) {
                    (item.id == 'schedulingInterPollDelay') && (item.id = 'schedulingInterPollDelay[count]');
                    (item.id == 'changesInterPollDelay') && (item.id = 'changesInterPollDelay[count]');
                });
                basicForm.markInvalid(responseText.errors);
                formErrorsPanel.show();
            }
        }
    }
});
