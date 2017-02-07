/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.ComServerEdit', {
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
        'setup.comserver.ComServerEdit'
    ],

    stores: [
        'ComServers',
        'Mdc.store.LogLevels',
        'Mdc.store.TimeUnitsWithoutMilliseconds'
    ],

    refs: [
        {
            ref: 'comServerEditPage',
            selector: 'comServerEdit'
        }
    ],

    init: function () {
        this.control({
            'comServerEdit button[action=saveModel]': {
                click: this.saveComServer
            }
        });
    },

    showEditView: function (id) {
        var me = this,
            widget = Ext.widget('comServerEdit'),
            model = me.getModel('Mdc.model.ComServer'),
            logLevelsStore = this.getStore('Mdc.store.LogLevels');

        logLevelsStore.load();
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setEdit(true, '#/administration/comservers');

        widget.setLoading(true);

        model.load(id, {
            success: function (record) {
                var comServerType = record.get('comServerType'),
                    form = widget.down('form'),
                    title;

                me.comServerModel = record;

                me.getApplication().fireEvent('loadComServer', record);

                switch (comServerType) {
                    case 'Online':
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

    showOnlineAddView: function () {
        var me = this,
            widget = Ext.widget('comServerEdit'),
            model = Ext.create(Mdc.model.ComServer),
            form;

        me.getApplication().fireEvent('changecontentevent', widget);
        form = widget.down('form');
        form.setTitle(Uni.I18n.translate('comServer.addOnline', 'MDC', 'Add online communication server'));
        widget.setEdit(false, '#/administration/comservers');

        model.beginEdit();
        model.set('comServerType', 'Online');
        model.set('serverLogLevel', 'Warning');
        model.set('communicationLogLevel', 'Warning');
        model.set('changesInterPollDelay', {count: '5', timeUnit: 'minutes'});
        model.set('schedulingInterPollDelay', {count: '60', timeUnit: 'seconds'});
        model.set('storeTaskQueueSize', 50);
        model.set('numberOfStoreTaskThreads', 1);
        model.set('storeTaskThreadPriority', 5);

        model.set('eventRegistrationPort', 8888);

        model.endEdit();
        me.comServerModel = model;
        me.modelToForm(model, form);
    },

    saveComServer: function (button) {
        var me = this,
            page = me.getComServerEditPage(),
            form = page.down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            model;

        try {
            model = me.formToModel();
            button.setDisabled(true);
            page.setLoading('Saving...');
            formErrorsPanel.hide();
            form.getForm().clearInvalid();
            model.save({
                backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/comservers').buildUrl(),
                callback: function (model, operation, success) {
                    page.setLoading(false);
                    button.setDisabled(false);

                    if (success) {
                        me.onSuccessSaving(operation.action, model.get('comServerType'));
                    } else {
                        var json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {

                            if(form.down('#num-event-uri-port').value == null)
                                form.down('#num-event-uri-port').markInvalid(Uni.I18n.translate('general.required.field', 'MDC', 'This field is required'));

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

        logLevelsStore.load(function () {
            timeUnitsStore.load(function () {
                basicForm.setValues(values);
            });
        });
    },

    formToModel: function () {
        var form = this.getComServerEditPage().down('form'),
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
                    case 'Online':
                        messageText = Uni.I18n.translate('comServer.acknowledge.createSuccess', 'MDC', 'Online communication server added');
                        break;
                }
                break;
            case 'update':
                switch (comServerType) {
                    case 'Online':
                        messageText = Uni.I18n.translate('comServer.acknowledge.updateSuccess', 'MDC', 'Online communication server saved');
                        break;
                }
                break;
        }
        this.getApplication().fireEvent('acknowledge', messageText);
        router.getRoute('administration/comservers').forward();
    },

    onFailureSaving: function (response) {
        var form = this.getComServerEditPage().down('form'),
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
