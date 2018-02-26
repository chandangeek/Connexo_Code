/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.controller.DataQualityKpiManagement', {
    extend: 'Cfg.insight.dataqualitykpi.controller.DataQualityKpiAdd',

    views: [
        'Cfg.insight.dataqualitykpi.view.AddManagement',
        'Cfg.insight.dataqualitykpi.view.DetailsDataQualityKpi'
    ],

    stores: [
        'Cfg.store.AllTasks',
        'Cfg.insight.dataqualitykpi.store.DataQualityKpis'
    ],

    refs: [
        {ref: 'dataValidationKpiEditForm', selector: 'ins-data-quality-kpi-add-mgm'},
        {ref: 'dataQualityKpiForm', selector: 'ins-data-quality-kpi-add-mgm'}
    ],

    init: function () {
        var me = this;

        if (Uni.util.Application.getAppName() == 'MdmApp') {
            Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
                name: Uni.I18n.translate('general.dataqualitykpi.', 'CFG', 'Data quality KPI'),
                controller: this
            });
        }

        me.control({
            'ins-data-quality-kpi-add-mgm #cmb-usage-point-group': {
                change: me.onUsagePointGroupChange
            },
            'ins-data-quality-kpi-add-mgm #add-button': {
                click: me.addDataQualityKpi
            }
        });
    },

    canAdministrate: function () {
        return Cfg.privileges.Validation.canAdministerDataQuality();
    },

    canView: function () {
        return Cfg.privileges.Validation.canViewResultsOrAdministerDataQuality();
    },

    canRun: function () {
        return false;
    },

    canEdit: function () {
        return Cfg.privileges.Validation.canAdministerDataQuality();
    },

    canSetTriggers: function () {
        return Cfg.privileges.Validation.canAdministerDataQuality();
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

    getTaskForm: function (caller, completedFunc) {
        var me = this,
            form = Ext.create('Cfg.insight.dataqualitykpi.view.AddManagement'),
            usagePointGroup = form.down('combobox[name=usagePointGroup]').getStore(),
            followByStore = form.down('#followedBy-combo').getStore();

        followByStore.load({
            callback: function () {
                usagePointGroup.load({
                    callback: function () {
                        completedFunc.call(caller, form);
                        form.loadRecord(Ext.create('Cfg.insight.dataqualitykpi.model.DataQualityKpi'));
                    }
                });
            }
        });
        return form;
    },

    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = me.getDataValidationKpiEditForm(),
            record = editForm.getRecord() || Ext.create('Cfg.insight.dataqualitykpi.model.DataQualityKpi'),
            frequency = editForm.down('[name=frequency]').getValue(),
            backUrl = router.getRoute('administration/dataqualitykpis').buildUrl(),
            usagePointGroup = {
                id: editForm.down('[name=usagePointGroup]').getValue()
            };

        editForm.getForm().clearInvalid();
        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
        }

        editForm.setLoading();

        editForm.updateRecord();
        record.beginEdit();
        if (!record.getId()) {
            record.set('usagePointGroup', usagePointGroup);
        } else {
            record.set('usagePointGroup', {
                id: record.get('usagePointGroup').id,
                name: record.get('usagePointGroup').name
            });
        }
        if (frequency) {
            record.set('frequency', me.getStore('Cfg.store.DataValidationKpiFrequency').getById(frequency).get('value'));
        }

        // set selected tasks
        var selectedTask = [];
        Ext.Array.each(editForm.down('#followedBy-combo').getValue(), function (value) {
            selectedTask.push({id: value});
        });
        record.nextRecurrentTasksStore = Ext.create('Ext.data.Store', {
            fields: ['id'],
            data: selectedTask
        });

        record.endEdit();
        record.save({
            success: function (record, operation) {
                saveOperationComplete.call(controller);
                switch (operation.action) {
                    case 'update':
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataqualitykpis.saved', 'CFG', 'Data quality KPI saved'));
                        break;
                    case 'create':
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataqualitykpis.eddited', 'CFG', 'Data quality KPI added'));
                        break;
                }
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    formErrorsPanel.show();
                    if (!Ext.isEmpty(operation.response.responseText)) {
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            Ext.each(json.errors, function (error) {
                                if (error.id === 'endUsagePointGroup') {
                                    editForm.down('[name=usagePointGroup]').markInvalid(error.msg);
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

    editTaskManagement: function (taskManagementId, formErrorsPanel,
                                  operationStartFunc, editOperationCompleteLoading,
                                  operationCompletedFunc, setTitleFunc, controller) {
        var me = this,
            form = me.getDataValidationKpiEditForm();

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/dqk/usagePointKpis/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.insight.dataqualitykpi.store.DataQualityKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    setTitleFunc.call(controller, record.get('usagePointGroup').name);
                    Ext.suspendLayouts();
                    form.loadRecord(record);
                    form.down('[name=usagePointGroup]').disable();
                    form.down('[name=frequency]').disable();
                    form.down('#view-purpose').disable();
                    Ext.resumeLayouts(true);
                    editOperationCompleteLoading.call(controller)
                });
            }
        })
    },

    historyTaskManagement: function (taskManagement) {
        return false;
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/dqk/usagePointKpis/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.insight.dataqualitykpi.store.DataQualityKpis');
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
            url: '/api/dqk/usagePointKpis/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.insight.dataqualitykpi.store.DataQualityKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    Ext.create('Uni.view.window.Confirmation').show({
                        title: Uni.I18n.translate('general.removex.kpi', 'CFG', "Remove '{0}'?", [record.get('usagePointGroup').name]),
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
            widget = Ext.widget('ins-data-quality-kpi-details', {
                actionMenu: actionMenu,
                canAdministrate: me.canAdministrate()
            });

        pageMainContent.setLoading(true);

        Ext.Ajax.request({
            url: '/api/dqk/usagePointKpis/recurrenttask/' + taskManagementRecord.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Cfg.insight.dataqualitykpi.store.DataQualityKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.down('#data-collection-kpi-details-side-menu').setHeader(record.get('usagePointGroup').name);
                    me.getApplication().fireEvent('loadTask', record.get('usagePointGroup').name);

                    var frequency = record.get('frequency');
                    widget.setRecurrentTasks('#followedBy-field-container', record.get('nextRecurrentTasks'));
                    widget.setRecurrentTasks('#precededBy-field-container', record.get('previousRecurrentTasks'));

                    widget.down('#cmb-usage-point-group').setValue(record.get('usagePointGroup').name);
                    widget.down('#view-purpose').setValue(record.get('metrologyPurpose').name);
                    widget.down('#data-quality-kpi-frequency').setValue(frequency ? Uni.util.ScheduleToStringConverter.convert(frequency) : '');
                    widget.down('#' + actionMenu.itemId) && (widget.down('#' + actionMenu.itemId).record = taskManagementRecord);
                });
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        })
    }
});
