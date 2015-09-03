Ext.define('Mdc.controller.setup.ComPortPoolEdit', {
    extend: 'Ext.app.Controller',

    models: [
        'Mdc.model.ComPortPool'
    ],

    requires: [
        'Mdc.store.DeviceDiscoveryProtocols'
    ],

    views: [
        'Mdc.view.setup.comportpool.ComPortPoolEdit'
    ],

    stores: [
        'Mdc.store.ComPortPools',
        'Mdc.store.ComPortTypes',
        'Mdc.store.ComPortTypesWithOutServlet',
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
        this.showAddView('Inbound');
    },

    showOutboundAddView: function () {
        this.showAddView('Outbound');
    },

    showAddView: function (type) {
        var me = this,
            widget = Ext.widget('comPortPoolEdit'),
            model = Ext.create(Mdc.model.ComPortPool),
            protocolDetectionCombo = widget.down('combobox[name=discoveryProtocolPluggableClassId]'),
            isInbound = false,
            form,
            title;

        switch (type.toLowerCase()) {
            case 'inbound':
                title = Uni.I18n.translate('comPortPool.title.addInbound', 'MDC', 'Add inbound communication port pool');
                me.getStore('Mdc.store.ComPortTypes').load();
                widget.down('form combobox[name=comPortType]').bindStore(me.getStore('Mdc.store.ComPortTypes'));
                isInbound = true;
                break;
            case 'outbound':
                title = Uni.I18n.translate('comPortPool.title.addOutbound', 'MDC', 'Add outbound communication port pool');
                me.getStore('Mdc.store.ComPortTypesWithOutServlet').load();
                widget.down('form combobox[name=comPortType]').bindStore(me.getStore('Mdc.store.ComPortTypesWithOutServlet'));
                break;
        }

        if (isInbound) {
            protocolDetectionCombo.show();
            protocolDetectionCombo.enable();
            protocolDetectionCombo.getStore().load();
        } else {
            protocolDetectionCombo.hide();
            protocolDetectionCombo.disable();
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

    showEditView: function (id) {
        var me = this,
            widget = Ext.widget('comPortPoolEdit'),
            model = me.getModel('Mdc.model.ComPortPool');

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setEdit(true, '#/administration/comportpools');

        widget.setLoading(true);

        model.load(id, {
            success: function (record) {
                var comServerType = record.get('comServerType'),
                    form = widget.down('form'),
                    isInbound = (record.get('direction').toLowerCase() === 'inbound'),
                    protocolDetectionCombo = form.down('combobox[name=discoveryProtocolPluggableClassId]'),
                    title;

                me.getApplication().fireEvent('loadComPortPool', record);

                title = Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'",[record.get('name')]);

                form.setTitle(title);
                if (isInbound) {
                    protocolDetectionCombo.show();
                    protocolDetectionCombo.enable();
                    protocolDetectionCombo.getStore().load();
                } else {
                    protocolDetectionCombo.hide();
                    protocolDetectionCombo.disable();
                }

                form.down('[name=direction_visual]').show();
                form.down('[name=comPortType]').setDisabled(true);
                form.loadRecord(record);
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
    },

    saveComPortPool: function (button) {
        var me = this,
            page = me.getComPortPoolEditPage(),
            form = page.down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            record;

        form.updateRecord();
        record = form.getRecord();
        button.setDisabled(true);
        page.setLoading(Uni.I18n.translate('general.saving', 'MDC', 'Saving...'));

        record.save({
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
    },

    onSuccessSaving: function (action, direction) {
        var router = this.getController('Uni.controller.history.Router'),
            messageText;

        switch (action) {
            case 'create':
                switch (direction.toLowerCase()) {
                    case 'inbound':
                        messageText = Uni.I18n.translate('comPortPool.acknowledge.inboundCreateSuccess', 'MDC', 'Inbound communication port pool added');
                        break;
                    case 'outbound':
                        messageText = Uni.I18n.translate('comPortPool.acknowledge.outboundCreateSuccess', 'MDC', 'Outbound communication port pool added');
                        break;
                }
                break;
            case 'update':
                switch (direction.toLowerCase()) {
                    case 'inbound':
                        messageText = Uni.I18n.translate('comPortPool.acknowledge.updateInboundSuccess', 'MDC', 'Inbound communication port pool saved');
                        break;
                    case 'outbound':
                        messageText = Uni.I18n.translate('comPortPool.acknowledge.updateOutboundSuccess', 'MDC', 'Outbound communication port pool saved');
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
