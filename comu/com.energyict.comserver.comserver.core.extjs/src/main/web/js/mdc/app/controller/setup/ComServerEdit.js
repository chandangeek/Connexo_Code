Ext.define('Mdc.controller.setup.ComServerEdit', {
    extend: 'Ext.app.Controller',

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
            model = me.getModel('Mdc.model.ComServer');

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
                        title = Uni.I18n.translate('comServer.title.editOnline', 'MDC', 'Edit online communication server');
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
        var widget = Ext.widget('comServerEdit'),
            model = Ext.create(Mdc.model.ComServer),
            form;

        model.set('comServerType', 'Online');

        this.comServerModel = model;

        this.getApplication().fireEvent('changecontentevent', widget);
        form = widget.down('form');
        form.setTitle(Uni.I18n.translate('comServer.title.addOnline', 'MDC', 'Add online communication server'));
        this.modelToForm(model, form);
        widget.setEdit(false, '#/administration/comservers');
    },

    saveComServer: function (button) {
        var me = this,
            page = me.getComServerEditPage(),
            form = page.down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            model;

        if (form.getForm().isValid()) {
            model = me.formToModel();

            button.setDisabled(true);
            page.setLoading('Saving...');
            formErrorsPanel.hide();
            model.save({
                callback: function (model, operation, success) {
                    page.setLoading(false);
                    button.setDisabled(false);

                    if (success) {
                        me.onSuccessSaving(operation.action, model.get('comServerType'));
                    } else {
                        me.onFailureSaving(operation.response);
                    }
                }
            });
        } else {
            formErrorsPanel.show();
        }
    },

    modelToForm: function (model, form) {
        var data = model.getData(),
            basicForm = form.getForm(),
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

        basicForm.setValues(values);
    },

    formToModel: function () {
        var form = this.getComServerEditPage().down('form'),
            queryString = Ext.Object.toQueryString(form.getValues()),
            values = Ext.Object.fromQueryString(queryString, true),
            model = this.comServerModel;

        model.beginEdit();
        model.set(values);
        model.endEdit();

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
