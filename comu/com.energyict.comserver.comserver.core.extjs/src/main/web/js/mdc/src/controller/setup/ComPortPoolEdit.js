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
            selector: '#comPortPoolEdit'
        }
    ],

    isEdit: false,
    editRecord: null,

    init: function () {
        this.control({
            'comPortPoolEdit button[action=saveModel]': {
                click: this.saveComPortPool
            },
            '#cbo-comportpool-protocol-detect': {
                select: this.loadProperties
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

        this.isEdit = false;

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

    loadProperties: function(combo, selectedRecords) {
        var editPage = this.getComPortPoolEditPage();
        editPage.down('#protocolDetectionDetails').setVisible(selectedRecords[0].properties().count() > 0);
        if(this.isEdit && !Ext.isEmpty(this.editRecord) && this.selectedEqualsRecord(selectedRecords[0])) {
            editPage.down('property-form').loadRecord(this.editRecord);
        } else {
            editPage.down('property-form').loadRecord(selectedRecords[0]);
        }
    },

    selectedEqualsRecord: function (record) {
        return record.get('id') === this.editRecord.get('discoveryProtocolPluggableClassId');
    },

    showEditView: function (id) {
        var me = this,
            widget = Ext.widget('comPortPoolEdit'),
            model = me.getModel('Mdc.model.ComPortPool');

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setEdit(true, '#/administration/comportpools');

        widget.setLoading(true);

        this.isEdit = true;

        widget.down('#cbo-comportpool-type').store.load(function () {
            model.load(id, {
                success: function (record) {
                    me.editRecord = record;
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
                    if(record.properties().count() > 0) {
                        form.down('property-form').loadRecord(record);
                    }
                    form.down('[name=comPortType]').setValue(record.get('comPortType').id);
                },
                callback: function () {
                    widget.setLoading(false);
                }
            });
        });
    },

    saveComPortPool: function (button) {
        var me = this,
            page = me.getComPortPoolEditPage(),
            form = page.down('form'),
            formErrorsPanel = form.down('uni-form-error-message'),
            record;

        if(!form.isValid()) {
            formErrorsPanel.show();
            return;
        }
        form.updateRecord();
        record = form.getRecord();
        form.down('property-form').updateRecord();
        record.propertiesStore = form.down('property-form').getRecord().properties();
        record.set('properties', form.down('property-form').getFieldValues().properties);
        button.setDisabled(true);
        page.setLoading(Uni.I18n.translate('general.saving', 'MDC', 'Saving...'));
        record.set('comPortType', form.down('#cbo-comportpool-type').findRecordByValue(form.down('#cbo-comportpool-type').getValue()).getData());
        record.save({
            backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/comportpools').buildUrl(),
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
