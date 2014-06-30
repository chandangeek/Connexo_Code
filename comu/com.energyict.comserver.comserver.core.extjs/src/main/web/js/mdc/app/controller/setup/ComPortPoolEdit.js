Ext.define('Mdc.controller.setup.ComPortPoolEdit', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.ComPortPool'
    ],

    views: [
        'Mdc.view.setup.comportpool.ComPortPoolEdit'
    ],

    stores: [
        'Mdc.store.ComPortPools',
        'Mdc.store.ComPortTypes',
        'Mdc.store.DeviceDiscoveryProtocols'
    ],

    refs: [
        {
            ref: 'comPortPoolEditPage',
            selector: 'comPortPoolEdit'
        }
    ],

    init: function () {
        this.control({
            'comPortPoolEdit button[action=saveModel]': {
                click: this.saveComPortPool
            }
        });
    },

    showInboundAddView: function () {
        this.showAddView('inbound');
    },

    showOutboundAddView: function () {
        this.showAddView('outbound');
    },

    showAddView: function (type) {
        var me = this,
            widget = Ext.widget('comPortPoolEdit'),
            model = Ext.create(Mdc.model.ComPortPool),
            isInbound = false,
            form,
            title;

        switch (type) {
            case 'inbound':
                title = Uni.I18n.translate('comPortPool.title.addInbound', 'MDC', 'Add inbound communication port pool');
                isInbound = true;
                break;
            case 'outbound':
                title = Uni.I18n.translate('comPortPool.title.addOutbound', 'MDC', 'Add outbound communication port pool');
                break;
        }

        model.set('direction', type);
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setEdit(false, '#/administration/comportpools');
        form = widget.down('form');
        form.setTitle(title);
        form.down('[name=discoveryProtocolPluggableClassId]').setVisible(isInbound);
        form.down('[name=discoveryProtocolPluggableClassId]').setDisabled(!isInbound);
        form.loadRecord(model);
    },

    saveComPortPool: function (button) {
        var me = this,
            page = me.getComPortPoolEditPage(),
            form = page.down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            model;

        if (form.getForm().isValid()) {
            form.updateRecord();
            model = form.getRecord();

            button.setDisabled(true);
            page.setLoading(Uni.I18n.translate('general.saving', 'MDC', 'Saving...'));
            formErrorsPanel.hide();

            model.save({
                callback: function (model, operation, success) {
                    page.setLoading(false);
                    button.setDisabled(false);

                    if (success) {
                        me.onSuccessSaving(operation.action, model.get('direction'));
                    } else {
                        me.onFailureSaving(operation.response);
                    }
                }
            });
        } else {
            formErrorsPanel.show();
        }
    },

    onSuccessSaving: function (action, direction) {
        var router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (action) {
            case 'create':
                switch (direction) {
                    case 'inbound':
                        messageText = Uni.I18n.translate('comPortPool.acknowledge.inboundCreateSuccess', 'MDC', 'Inbound communication port pool added');
                        break;
                    case 'outbound':
                        messageText = Uni.I18n.translate('comPortPool.acknowledge.outboundCreateSuccess', 'MDC', 'Outbound communication port pool saved');
                        break;
                }
                break;
        }
        this.getApplication().fireEvent('acknowledge', messageText);
        router.getRoute('administration/comportpools').forward();
    },

    onFailureSaving: function (response) {
        var form = this.getComPortPoolEditPage().down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            basicForm = form.getForm(),
            responseText;

        if (response.status == 400) {
            responseText = Ext.decode(response.responseText, true);
            if (responseText && responseText.errors) {
                basicForm.markInvalid(responseText.errors);
                formErrorsPanel.show();
            }
        }
    }
});
