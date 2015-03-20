Ext.define('Mdc.controller.setup.DataCollectionKpi', {
    extend: 'Ext.app.Controller',
    stores: [
        'Mdc.store.DataCollectionKpis',
        'Mdc.store.DataCollectionKpiFrequency',
        'Mdc.store.MeterExportGroups'
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

    saveModel: function (btn) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = me.getDataCollectionKpiEditForm(),
            record = editForm.getRecord(),
            connectionKpiField = editForm.down('#connectionKpiField'),
            communicationKpiField = editForm.down('#communicationKpiField'),
            deviceGroupCombo = editForm.down('#deviceGroupCombo'),
            frequencyCombo = editForm.down('#frequencyCombo'),
            successMessage = Uni.I18n.translate('datacollectionkpis.saved', 'MDC', 'Data collection KPI saved'),
            kpiMessageContainer = me.getKpiErrorContainer();

        kpiMessageContainer.hide();
        deviceGroupCombo.clearInvalid();
        me.showErrorPanel(false);
        editForm.setLoading();
        record.beginEdit();
        record.set('communicationTarget', communicationKpiField.getValue());
        record.set('connectionTarget', connectionKpiField.getValue());

        if (btn.action === 'add') {
            successMessage = Uni.I18n.translate('datacollectionkpis.added', 'MDC', 'Data collection KPI added');
            if (deviceGroupCombo.getValue() === '') {
                record.set('deviceGroup', null);
            } else {
                record.set('deviceGroup', { id: deviceGroupCombo.getValue() });
            }
            if (frequencyCombo.getValue() === '') {
                record.set('frequency', {});
            } else {
                record.set('frequency', frequencyCombo.getValue());
            }
        }
        record.endEdit();
        record.save({
            success: function (record) {
                router.getRoute('administration/datacollectionkpis').forward();
                me.getApplication().fireEvent('acknowledge', successMessage);
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    me.showErrorPanel(true);
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            Ext.each(json.errors, function (error) {
                                if (error.id === 'communicationKpi') {
                                    kpiMessageContainer.update(error.msg);
                                    kpiMessageContainer.show();
                                }
                                if (error.id === 'endDeviceGroup') {
                                    deviceGroupCombo.markInvalid(error.msg);
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
            deviceGroupStore = widget.down('combobox[name=deviceGroup]').getStore(),
            kpiModel = Ext.ModelManager.getModel('Mdc.model.DataCollectionKpi'),
            form = widget.down('#dataCollectionKpiEditForm'),
            deviceGroupCombo = widget.down('#deviceGroupCombo'),
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
                            var connectionKpiField = widget.down('#connectionKpiField'),
                                connectionKpiDisplayField = widget.down('#connectionKpiDisplayField'),
                                communicationKpiField = widget.down('#communicationKpiField'),
                                communicationKpiDisplayField = widget.down('#communicationKpiDisplayField'),
                                frequencyCombo = widget.down('#frequencyCombo'),
                                frequencyDisplayField = widget.down('#frequencyDisplayField'),
                                editTitle = Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + kpiRecord.get('deviceGroup').name + "'",
                                frequencyStore = frequencyCombo.getStore(),
                                frequencyObject = kpiRecord.get('frequency').every;

                            Ext.suspendLayouts();

                            form.loadRecord(kpiRecord);

                            widget.down('#dataCollectionKpiEditForm').setTitle(editTitle);
                            me.getApplication().fireEvent('loadDataCollectionKpi', editTitle);
                            createBtn.setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
                            createBtn.action = 'save';

                            if (!Ext.isEmpty(kpiRecord.get('connectionTarget'))) {
                                connectionKpiField.setValue(kpiRecord.get('connectionTarget'));
                                connectionKpiField.hide();
                                connectionKpiDisplayField.show();
                            } else {
                                connectionKpiField.setValue(null);
                            }

                            if (!Ext.isEmpty(kpiRecord.get('communicationTarget'))) {
                                communicationKpiField.setValue(kpiRecord.get('communicationTarget'));
                                communicationKpiField.hide();
                                communicationKpiDisplayField.show();
                            } else {
                                communicationKpiField.setValue(null);
                            }

                            deviceGroupDisplayField.setValue(kpiRecord.get('deviceGroup').name);
                            deviceGroupDisplayField.show();
                            deviceGroupCombo.hide();

                            frequencyDisplayField.show();
                            frequencyCombo.hide();
                            frequencyStore.each(function(record) {
                                var value = record.get('every').every;
                                if (value.count === frequencyObject.count && value.timeUnit === frequencyObject.timeUnit) {
                                    frequencyDisplayField.setValue(record.get('name'));
                                }
                            });

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
    }
});
