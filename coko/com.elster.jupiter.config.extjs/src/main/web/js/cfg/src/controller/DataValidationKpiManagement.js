/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.controller.DataValidationKpiManagement', {
    extend: 'Cfg.controller.DataValidationKpi',

    views: [
        'Cfg.view.datavalidationkpis.Add',
        'Cfg.view.taskmanagement.DetailsDataQualityKpi'
    ],
    refs: [
        {ref: 'dataValidationKpiEditForm', selector: 'cfg-data-validation-kpi-add-mgm'}
    ],

    init: function () {
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.dataqualitykpi.', 'CFG', 'Data quality KPI'),
            controller: this
        });
    },

    canAdministrate: function () {
        return Cfg.privileges.Validation.canAdministerDataQuality();
    },

    canRun: function () {
        return false;
    },

    canEdit: function () {
        return false;
    },

    canHistory: function () {
        return false;
    },

    canRemove: function () {
        return Cfg.privileges.Validation.canAdministerDataQuality();
    },

    getType: function () {
        return 'DataQualityKpiCalcTopic';
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

    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
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
                //me.getController('Uni.controller.history.Router').getRoute(me.getController('Uni.controller.history.Router').currentRoute.replace('/add', '')).forward();
                saveOperationComplete.call(controller);
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
    },

    runTaskManagement: function (taskManagement) {
    },

    editTaskManagement: function (taskManagement) {
    },

    historyTaskManagement: function (taskManagement) {
        return false;
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/dqk/deviceKpis/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.store.DataValidationKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    operationCompleted.call(controller, me, taskManagementId, record);
                });
            }
        })
    },

    removeTaskManagement: function (taskManagement, startRemovingFunc, removeCompleted, controller) {
        var me = this;

        startRemovingFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/dqk/deviceKpis/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.store.DataValidationKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    Ext.create('Uni.view.window.Confirmation').show({
                        title: Uni.I18n.translate('general.removex.kpi', 'CFG', "Remove '{0}'?", [record.get('deviceGroup').name]),
                        msg: Uni.I18n.translate('dataqualitykpis.deleteConfirmation.msg', 'CFG', 'This data quality KPI will no longer be available in the system. Already calculated data will not be removed.'),
                        fn: function (state) {
                            switch (state) {
                                case 'confirm':
                                    me.removeOperation(record, startRemovingFunc, removeCompleted, controller);
                                    break;
                            }
                        }
                    });
                });
            }
        })
    },

    removeOperation: function (record, startRemovingFunc, removeCompleted, controller) {
        var me = this;

        record.destroy({
            success: function () {
                removeCompleted.call(controller, true);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataqualitykpis.removed', 'CFG', 'Data quality KPI removed'));
            },
            failure: function (record, operation) {
                removeCompleted.call(controller, false);
                if (operation.response.status === 409) {
                    return;
                }
            }
        });
    },

    viewTaskManagement: function (taskId, actionMenu, taskManagementRecord) {
        var me = this,
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget = Ext.widget('data-quality-kpi-details', {
                actionMenu: actionMenu
            });

        pageMainContent.setLoading(true);

        Ext.Ajax.request({
            url: '/api/dqk/deviceKpis/recurrenttask/' + taskManagementRecord.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.store.DataValidationKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    var frequency = record.get('frequency')

                    widget.down('#data-quality-kpi-device-group').setValue(record.get('deviceGroup').name);
                    widget.down('#data-quality-kpi-frequency').setValue(frequency ? Uni.util.ScheduleToStringConverter.convert(frequency) : '');
                    widget.down('#' + actionMenu.itemId).record = taskManagementRecord;
                });
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        })

    }
});
