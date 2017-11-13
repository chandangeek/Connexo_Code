/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.controller.DataValidationKpiManagement', {
    extend: 'Cfg.controller.DataValidationKpi',

    views: [
        'Cfg.view.datavalidationkpis.Add'
    ],
    refs: [
        {ref: 'dataValidationKpiEditForm', selector: 'cfg-data-validation-kpi-add-mgm'}
    ],

    init: function () {
        Apr.TaskManagementApp.addTaskManagementApp('DataQualityKpiCalcTopic', {
            name: Uni.I18n.translate('general.dataqualitykpi.', 'CFG', 'Data quality KPI'),
            controller: this
        });
    },

    getTaskForm: function () {
        var me = this,
            form = Ext.create('Cfg.view.taskmanagement.AddDataValidationKpiManagement'),
            deviceGroupStore = form.down('comboboxwithemptycomponent[name=deviceGroup]').getStore(),
            kpiModel = Ext.ModelManager.getModel('Cfg.model.DataValidationKpi'),
            deviceGroupCombo = form.down('#cmb-device-group');

        deviceGroupStore.load({
            callback: function () {
                if (deviceGroupStore.getCount() > 0) {
                    form.loadRecord(Ext.create(kpiModel));
                }
            }
        });
        return form;
    },


    saveTaskForm: function (panel, formErrorsPanel) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = me.getDataValidationKpiEditForm(),
            record = editForm.getRecord(),
            frequency = editForm.down('[name=frequency]').getValue(),
            backUrl = router.getRoute('administration/dataqualitykpis').buildUrl(),
            deviceGroup = {
                id: editForm.down('[name=deviceGroup]').getValue()
            };

        editForm.getForm().clearInvalid();
        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
        }

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
            record.set('frequency', me.getStore('Cfg.store.DataValidationKpiFrequency').getById(frequency).get('value'));
        }
        record.endEdit();
        record.save({
            backUrl: backUrl,
            success: function (record, operation) {
                me.getController('Uni.controller.history.Router').getRoute(me.getController('Uni.controller.history.Router').currentRoute.replace('/add', '')).forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataqualitykpis.added', 'CFG', 'Data quality KPI added'));
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    formErrorsPanel.show();
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            Ext.each(json.errors, function (error) {

                                if (error.id === 'endDeviceGroup') {
                                    editForm.down('[name=deviceGroup]').markInvalid(error.msg);
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
