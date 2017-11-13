/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.TaskManagementDataCollectionKpi', {
    extend: 'Mdc.controller.setup.DataCollectionKpi',
    views: [
        'Mdc.view.setup.taskmanagement.AddEditDataCollectionKpis'
    ],
    refs: [
        {ref: 'dataCollectionKpiEditForm', selector: '#dataCollectionKpiEditForm'}
    ],


    init: function () {
        this.control({
            '#dataCollectionKpiEditForm #cmb-frequency': {
                change: this.onFrequencyChange
            }
        });
        Apr.TaskManagementApp.addTaskManagementApp('MDCKpiCalculatorTopic', {
            name: Uni.I18n.translate('general.datadataCollectionKPI', 'MDC', 'Data collection KPI'),
            controller: this
        });
    },

    getTaskForm: function (id) {
        var me = this,
            form = Ext.create('Mdc.view.setup.taskmanagement.AddEditDataCollectionKpis'),
            deviceGroupStore = form.down('combobox[name=deviceGroup]').getStore(),
            kpiModel = Ext.ModelManager.getModel('Mdc.model.DataCollectionKpi'),
            deviceGroupCombo = form.down('#cmb-device-group'),
            deviceGroupDisplayField = form.down('#devicegroupDisplayField'),
            createBtn = form.down('#createEditButton');

        //me.getApplication().fireEvent('changecontentevent', widget);
        //widget.setLoading(true);
        deviceGroupStore.load({
            callback: function () {
                if (!Ext.isEmpty(id)) {
                    //widget.down('#dataCollectionKpiEditForm').setTitle(Uni.I18n.translate('datacollectionkpis.editDataCollectionKpi', 'MDC', 'Edit data collection KPI'));
                    kpiModel.load(id, {
                        success: function (kpiRecord) {
                            Ext.suspendLayouts();
                            form.loadRecord(kpiRecord);
                            form.down('[name=deviceGroup]').disable();
                            form.down('[name=frequency]').disable();
                            //widget.down('#dataCollectionKpiEditForm').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", kpiRecord.get('deviceGroup').name));
                            me.getApplication().fireEvent('loadDataCollectionKpi', kpiRecord.get('deviceGroup').name);
                            createBtn.setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
                            createBtn.action = 'save';
                            Ext.resumeLayouts(true);

                            //widget.setLoading(false);
                        }
                    });
                } else {
                    if (deviceGroupStore.getCount() > 0) {
                        form.loadRecord(Ext.create(kpiModel));
                    } else {
                        Ext.suspendLayouts();
                        deviceGroupDisplayField.setValue('<span style="color: #eb5642">' + Uni.I18n.translate('datacollectionkpis.noDeviceGroup', 'MDC', 'No device group defined yet.') + '</span>');
                        deviceGroupDisplayField.show();
                        deviceGroupCombo.hide();
                        createBtn.disable();
                        Ext.resumeLayouts(true);
                    }
                    //widget.down('#dataCollectionKpiEditForm').setTitle(Uni.I18n.translate('datacollectionkpis.add', 'MDC', 'Add data collection KPI'));
                    //  widget.setLoading(false);
                }
            }
        });
        return form;
    },

    saveTaskForm: function (panel, formErrorsPanel) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = panel.down('data-collection-kpi-addedit-tgm'),
            record = editForm.getRecord(),
            deviceGroup = {
                id: editForm.down('[name=deviceGroup]').getValue()
            },
            frequency = editForm.down('[name=frequency]').getValue(),
            displayRange = editForm.down('[name=displayRange]').getValue(),
            connectionTarget = editForm.down('#connectionKpiField').getValue(),
            kpiMessageContainer = editForm.down('#kpiErrorContainer'),
            communicationTarget = editForm.down('#communicationKpiField').getValue();

        kpiMessageContainer.hide();
        formErrorsPanel.hide();

        editForm.setLoading();
        record.beginEdit();
        if (!record.getId()) {
            record.set('deviceGroup', deviceGroup);
        } else {
            record.set('deviceGroup', {
                id: record.get('deviceGroup').id,
                name: record.get('deviceGroup').name
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
            success: function (record, operation) {
                var successMessage = '';

                switch (operation.action) {
                    case 'update':
                        successMessage = Uni.I18n.translate('datacollectionkpis.saved', 'MDC', 'Data collection KPI saved');
                        break;
                    case 'create':
                        successMessage = Uni.I18n.translate('datacollectionkpis.added', 'MDC', 'Data collection KPI added');
                        break;
                }

                me.getController('Uni.controller.history.Router').getRoute(me.getController('Uni.controller.history.Router').currentRoute.replace('/add', '')).forward();
                me.getApplication().fireEvent('acknowledge', successMessage);
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    formErrorsPanel.show();
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            Ext.each(json.errors, function (error) {
                                if (error.id === 'communicationKpi') {
                                    kpiMessageContainer.update(error.msg);
                                    formErrorsPanel.show();
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
            callback: function () {
                editForm.setLoading(false);
            }
        });
    }
});
