Ext.define('Mdc.controller.setup.DataCollectionKpi', {
    extend: 'Ext.app.Controller',
    stores: [
        'Mdc.store.DataCollectionKpis',
        'Mdc.store.DataCollectionKpiFrequency',
        'Mdc.store.MeterExportGroups',
        'Mdc.store.DataCollectionKpiRange'
    ],
    models: [
        'Mdc.model.DataCollectionKpi'
    ],

    views: [
        'Mdc.view.setup.datacollectionkpis.Setup',
        'Mdc.view.setup.datacollectionkpis.Edit'
    ],
    refs: [
        {ref: 'dataCollectionKpisPreviewForm', selector: '#dataCollectionKpisDetailsForm'},
        {ref: 'dataCollectionKpisGrid', selector: '#datacollectionkpisgrid'},
        {ref: 'dataCollectionKpiEditForm', selector: '#dataCollectionKpiEditForm'},
        {ref: 'dataCollectionKpisPreviewContainer', selector: '#datacollectionkpispreview'},
        {ref: 'kpiErrorContainer', selector: '#kpiErrorContainer'}
    ],


    init: function () {
        this.control({
            '#dataCollectionKpiSetup #datacollectionkpisgrid': {
                select: this.showKpiPreview
            },
            '#dataCollectionKpiSetup button[action=addDataCollectionKpi]': {
                click: this.moveToCreatePage
            },
            'dataCollectionKpisActionMenu menuitem[action=edit]': {
                click: this.moveToEditPage
            },
            '#dataCollectionKpiEdit button[action=cancelAction]': {
                click: this.moveToViewPage
            },
            '#dataCollectionKpiEdit #createEditButton': {
                click: this.saveModel
            },
            'dataCollectionKpiEdit #cmb-frequency': {
                change: this.onFrequencyChange
            }
        });
    },

    moveToViewPage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/datacollectionkpis').forward();
    },

    moveToCreatePage: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/datacollectionkpis/add').forward();
    },

    moveToEditPage: function () {
        var grid = this.getDataCollectionKpisGrid(),
            router = this.getController('Uni.controller.history.Router'),
            record = grid.getSelectionModel().getLastSelected();

        router.getRoute('administration/datacollectionkpis/edit').forward({id: record.get('id')});
    },

    showKpiPreview: function (selectionModel) {
        var previewForm = this.getDataCollectionKpisPreviewForm(),
            previewContainer = this.getDataCollectionKpisPreviewContainer(),
            record = selectionModel.getLastSelected();

        if (previewForm && record) {
            previewContainer.setTitle(Uni.I18n.translate('general.general', 'MDC', 'General'));
            previewForm.loadRecord(record);
        }
    },

    showErrorPanel: function (value) {
        var editForm = this.getDataCollectionKpiEditForm(),
            errorPanel = editForm.down('#form-errors');

        editForm.getForm().clearInvalid();
        errorPanel.setVisible(value);
    },

    saveModel: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = me.getDataCollectionKpiEditForm(),
            record = editForm.getRecord(),
            deviceGroup = {
                id: editForm.down('[name=devicegroup]').getValue()
            },
            frequency = editForm.down('[name=frequency]').getValue(),
            displayRange = editForm.down('[name=displayRange]').getValue(),
            connectionTarget = editForm.down('#connectionKpiField').getValue(),
            communicationTarget = editForm.down('#communicationKpiField').getValue(),
            successMessage = Uni.I18n.translate('datacollectionkpis.saved', 'MDC', 'Data collection KPI saved.'),
            kpiMessageContainer = me.getKpiErrorContainer();

        kpiMessageContainer.hide();
        me.showErrorPanel(false);
        editForm.setLoading();
        record.beginEdit();
        if (!record.getId()) {
            record.set('deviceGroup', deviceGroup);
        } else {
            record.set('deviceGroup', {
                id: record.get('deviceGroup').id
            });
        }
        if (frequency) {
            record.set('frequency', me.getStore('Mdc.store.DataCollectionKpiFrequency').getById(frequency).get('value'));
        }
        if (displayRange) {
            record.set('displayRange', me.getStore('Mdc.store.DataCollectionKpiRange').getById(displayRange).get('value'));
        }
        record.set('communicationTarget', communicationTarget);
        record.set('connectionTarget', connectionTarget);
        record.endEdit();
        record.save({
            success: function (record) {
                router.getRoute('administration/datacollectionkpis').forward();
                me.getApplication().fireEvent('acknowledge', successMessage);
            },
            failure: function (record, operation) {
                if(operation.response.status == 400) {
                    me.showErrorPanel(true);
                    if(!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            Ext.each(json.errors, function(error) {
                               if (error.id === 'communicationKpi') {
                                   kpiMessageContainer.update(error.msg);
                                   kpiMessageContainer.show();
                               }
                            });
                            editForm.getForm().markInvalid(json.errors);
                        }
                    }
                }
            },
            callback: function() {
                editForm.setLoading(false);
            }
        });
    },


    showDataCollectionKpiView: function () {
        var me = this,
            widget = Ext.widget('dataCollectionKpiSetup'),
            store = widget.down('#datacollectionkpisgrid').getStore();

        me.getApplication().fireEvent('changecontentevent', widget);
        store.load();
    },

    showDataCollectionKpiEditView: function (id) {
        var me = this,
            widget = Ext.widget('dataCollectionKpiEdit'),
            deviceGroupStore = widget.down('combobox[name=devicegroup]').getStore(),
            kpiModel = Ext.ModelManager.getModel('Mdc.model.DataCollectionKpi'),
            form = widget.down('#dataCollectionKpiEditForm'),
            deviceGroupCombo = widget.down('#cmb-device-group'),
            deviceGroupDisplayField = widget.down('#devicegroupDisplayField'),
            createBtn = widget.down('#createEditButton');

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        deviceGroupStore.load({
            callback: function () {
                if (!Ext.isEmpty(id)) {
                    widget.down('#dataCollectionKpiEditForm').setTitle(Uni.I18n.translate('datacollectionkpis.editDataCollectionKpi', 'MDC', 'Edit data collection KPI'));
                    kpiModel.load(id, {
                        success: function (kpiRecord) {
                            var editTitle = Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + kpiRecord.get('deviceGroup').name + "'";

                            Ext.suspendLayouts();
                            form.loadRecord(kpiRecord);
                            form.down('[name=devicegroup]').disable();
                            form.down('[name=frequency]').disable();
                            widget.down('#dataCollectionKpiEditForm').setTitle(editTitle);
                            me.getApplication().fireEvent('loadDataCollectionKpi', editTitle);
                            createBtn.setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
                            createBtn.action = 'save';
                            Ext.resumeLayouts(true);

                            widget.setLoading(false);
                        }
                    });
                } else {
                    if (deviceGroupStore.getCount() > 0) {
                        form.loadRecord(Ext.create(kpiModel));
                    } else {
                        deviceGroupDisplayField.setValue('<span style="color: #eb5642">' + Uni.I18n.translate('datacollectionkpis.noDeviceGroup', 'MDC', 'No device group defined yet.') +  '</span>' );
                        deviceGroupDisplayField.show();
                        deviceGroupCombo.hide();
                        createBtn.disable();
                    }
                    widget.down('#dataCollectionKpiEditForm').setTitle(Uni.I18n.translate('datacollectionkpis.addDataCollectionKpi', 'MDC', 'Add data collection KPI'));
                    widget.setLoading(false);
                }
            }
        });
    },

    onFrequencyChange: function (combo, newValue) {
        var me = this,
            displayRangeCombo = me.getDataCollectionKpiEditForm().down('#displayRangeCombo');

        Ext.suspendLayouts();
        if (newValue) {
            me.getStore('Mdc.store.DataCollectionKpiRange').filterByFrequency(newValue);
            displayRangeCombo.enable();
        } else {
            displayRangeCombo.disable();
        }
        displayRangeCombo.reset();
        Ext.resumeLayouts(true);
    }
});
