/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.TaskManagementDataCollectionKpi', {
    extend: 'Mdc.controller.setup.DataCollectionKpi',
    stores: [
        'Mdc.store.DataCollectionKpiType',
        'Mdc.store.AllTasks',
        'Mdc.store.AvailableDeviceGroups',
        'Mdc.crlrequest.store.SecurityAccessorsWithPurpose'
    ],
    views: [
        'Mdc.view.setup.taskmanagement.AddEditDataCollectionKpis',
        'Mdc.view.setup.taskmanagement.DetailsDataCollectionKpi'
    ],
    refs: [
        {ref: 'dataCollectionKpiEditForm', selector: 'data-collection-kpi-addedit-tgm'}
    ],

    init: function () {
        this.control({
            'data-collection-kpi-addedit-tgm #cmb-frequency': {
                change: this.onFrequencyChange
            },
            'data-collection-kpi-addedit-tgm #cmb-collectionType': {
                change: this.onCollectionTypeChange
            },
            'data-collection-kpi-addedit-tgm #cmb-device-group': {
                change: this.onGroupChange
            }
        });
        Apr.TaskManagementApp.addTaskManagementApp(this.getType(), {
            name: Uni.I18n.translate('general.datadataCollectionKPI', 'MDC', 'Data collection KPI'),
            controller: this
        });
    },

    canAdministrate: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    canView: function () {
        return Mdc.privileges.DataCollectionKpi.canView();
    },

    canRun: function () {
        return false;
    },

    canEdit: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    canSetTriggers: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    canHistory: function () {
        return false;
    },

    getType: function () {
        return 'MDCKpiCalculatorTopic';
    },

    canRemove: function () {
        return Mdc.privileges.DataCollectionKpi.canEdit();
    },

    getTaskForm: function (caller, completedFunc) {
        var me = this,
            form = Ext.create('Mdc.view.setup.taskmanagement.AddEditDataCollectionKpis'),
            followByStore = form.down('#followedBy-combo').getStore();

        followByStore.load({
            callback: function () {
                completedFunc.call(caller, form);
            }
        });

        return form;
    },

    saveTaskForm: function (panel, formErrorsPanel, saveOperationComplete, controller) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            editForm = panel.down('data-collection-kpi-addedit-tgm'),
            record = editForm.getRecord() || Ext.create('Mdc.model.DataCollectionKpi'),
            deviceGroup = {
                id: editForm.down('[name=deviceGroup]').getValue()
            },
            frequency = editForm.down('[name=frequency]').getValue(),
            displayRange = editForm.down('[name=displayRange]').getValue(),
            target = editForm.down('#kpi-target'),
            collectionTypeKpi = editForm.down('#cmb-collectionType');

        formErrorsPanel.hide();
        editForm.getForm().clearInvalid();
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


        // set selected tasks
        var selectedTask = [];
        Ext.Array.each(editForm.down('#followedBy-combo').getValue(), function (value) {
            selectedTask.push({id: value});
        });

        var nextRecurrentTasksStore = Ext.create('Ext.data.Store', {
            fields: ['id'],
            data: selectedTask
        });
        if (collectionTypeKpi.getValue() === 'connection') {
            record.set('connectionTarget', target.getValue());
            record.connectionNextRecurrentTasksStore = nextRecurrentTasksStore;
        }
        else {
            record.set('communicationTarget', target.getValue());
            record.communicationNextRecurrentTasksStore = nextRecurrentTasksStore;
        }


        record.endEdit();
        record.getProxy().url = '/api/ddr/kpis/' + collectionTypeKpi.getValue();
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

                //me.getController('Uni.controller.history.Router').getRoute(me.getController('Uni.controller.history.Router').currentRoute.replace('/add', '')).forward();
                saveOperationComplete.call(controller);
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
                                    editForm.down('#communicationKpiField #noTarget').markInvalid(error.msg);
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
    },

    runTaskManagement: function (taskManagement) {
    },

    editTaskManagement: function (taskManagementId, formErrorsPanel,
                                  operationStartFunc, editOperationCompleteLoading,
                                  operationCompletedFunc, setTitleFunc, controller) {
        var me = this,
            form = me.getDataCollectionKpiEditForm();

        operationStartFunc.call(controller);
        Ext.Ajax.request({
            url: '/api/ddr/kpis/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.store.DataCollectionKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    setTitleFunc.call(controller, record.get('deviceGroup').name);
                    Ext.suspendLayouts();


                    var nextRecurrentTasks = taskManagementId == record.get('communicationTaskId') ? record.get('communicationNextRecurrentTasks') : record.get('connectionNextRecurrentTasks');
                    if (nextRecurrentTasks) {
                        var selectedTasks = [];
                        Ext.Array.each(nextRecurrentTasks, function (nextRecurrentTask) {
                            selectedTasks.push(nextRecurrentTask.id);
                        });

                        form.down('[name=nextRecurrentTasks]').setValue(selectedTasks);
                    }

                    if (record.get('communicationTaskId') == taskManagementId) {
                        form.down('[name=collectionType]').setValue('communication');
                        form.down('[name=target]').setValue(record.get('communicationTarget'));
                    }
                    else if (record.get('connectionTaskId') == taskManagementId) {
                        form.down('[name=collectionType]').setValue('connection');
                        form.down('[name=target]').setValue(record.get('connectionTarget'));
                    }

                    form.down('[name=collectionType]').disable();
                    form.down('[name=deviceGroup]').disable();
                    form.down('[name=frequency]').disable();
                    form.loadRecord(record);
                    Ext.resumeLayouts(true);
                    editOperationCompleteLoading.call(controller)
                });
            }
        })
    },

    historyTaskManagement: function () {
        return false;
    },

    getTask: function (controller, taskManagementId, operationCompleted) {
        var me = this;

        Ext.Ajax.request({
            url: '/api/ddr/kpis/recurrenttask/' + taskManagementId,
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.store.DataCollectionKpis');
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
            url: '/api/ddr/kpis/recurrenttask/' + taskManagement.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.store.DataCollectionKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    var type = record.get('communicationTaskId') == taskManagement.get('id') ? 'communication' : 'connection';
                    Ext.create('Uni.view.window.Confirmation').show({
                        title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", [record.get('deviceGroup').name]),
                        msg: Uni.I18n.translate('datacollectionkpis.deleteConfirmation.msg', 'MDC', 'This data collection KPI will no longer be available on connections and communications overview.'),
                        fn: function (state) {
                            switch (state) {
                                case 'confirm':
                                    me.removeOperation(record, type, startRemovingFunc, removeCompleted, controller);
                                    break;
                            }
                        }
                    });
                });
            }
        })
    },

    removeOperation: function (record, type, startRemovingFunc, removeCompleted, controller) {
        var me = this;

        record.getProxy().url = '/api/ddr/kpis/' + type;
        record.destroy({
            success: function () {
                removeCompleted.call(controller, true);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('datacollectionkpis.kpiRemoved', 'MDC', 'Data collection KPI removed'));
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
            widget = Ext.widget('data-collection-kpi-details', {
                actionMenu: actionMenu,
                canAdministrate: me.canAdministrate()
            });

        pageMainContent.setLoading(true);

        Ext.Ajax.request({
            url: '/api/ddr/kpis/recurrenttask/' + taskManagementRecord.get('id'),
            method: 'GET',
            success: function (operation) {
                var response = Ext.JSON.decode(operation.responseText),
                    store = Ext.create('Mdc.store.DataCollectionKpis');
                store.loadRawData([response]);
                store.each(function (record) {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    me.getApplication().fireEvent('loadTask', record.get('deviceGroup').name);
                    widget.down('#data-collection-kpi-details-side-menu').setHeader(record.get('deviceGroup').name);
                    var connectionTarget = record.get('connectionTarget'),
                        communicationTarget = record.get('communicationTarget'),
                        displayRange = record.get('displayRange');

                    connectionTarget = connectionTarget != null ? connectionTarget + ' %' : Uni.I18n.translate('datacollectionkpis.noKpi', 'MDC', 'No KPI');
                    communicationTarget = communicationTarget != null ? communicationTarget + ' %' : Uni.I18n.translate('datacollectionkpis.noKpi', 'MDC', 'No KPI');
                    widget.down('#data-collection-kpi-device-group').setValue(record.get('deviceGroup').name);
                    widget.down('#data-collection-kpi-frequency').setValue(displayRange ? Ext.getStore('Mdc.store.DataCollectionKpiRange').getById(displayRange.count + displayRange.timeUnit).get('name') : '');
                    if (taskManagementRecord.get('id') == record.get('communicationTaskId')) {
                        widget.down('#kpi-target').setValue(communicationTarget);
                        widget.setRecurrentTasks('#followedBy-field-container', record.get('communicationNextRecurrentTasks'));
                        widget.setRecurrentTasks('#precededBy-field-container', record.get('communicationPreviousRecurrentTasks'));
                    }
                    else {
                        widget.down('#kpi-target').setValue(connectionTarget);
                        widget.setRecurrentTasks('#followedBy-field-container', record.get('connectionNextRecurrentTasks'));
                        widget.setRecurrentTasks('#precededBy-field-container', record.get('connectionPreviousRecurrentTasks'));
                    }


                    widget.down('#' + actionMenu.itemId) && (widget.down('#' + actionMenu.itemId).record = taskManagementRecord);
                });
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        })

    },

    onCollectionTypeChange: function (combo, newValue) {
        var me = this,
            form = me.getDataCollectionKpiEditForm(),
            deviceGroupCombo = form.down('#cmb-device-group'),
            deviceGroupStore = deviceGroupCombo.getStore(),
            deviceGroupDisplayField = form.down('#devicegroupDisplayField');

        new Ext.get('displayRangeSubTpl').setHTML('');
        new Ext.get('frequencySubTpl').setHTML('');

        deviceGroupCombo.clearValue();
        deviceGroupCombo.setDisabled(false);
        deviceGroupStore.getProxy().url = '/api/ddr/kpis/groups/' + newValue;
        deviceGroupStore.load({
            callback: function () {
                if (deviceGroupStore.getCount() == 0) {
                    Ext.suspendLayouts();
                    deviceGroupDisplayField.setValue('<span style="color: #eb5642">' + Uni.I18n.translate('datacollectionkpis.noDeviceGroup', 'MDC', 'No device group defined yet.') + '</span>');
                    deviceGroupDisplayField.show();
                    deviceGroupCombo.hide();
                    Ext.resumeLayouts(true);
                }
                else {
                    Ext.suspendLayouts();
                    deviceGroupDisplayField.hide();
                    deviceGroupCombo.show();
                    Ext.resumeLayouts(true);
                }
            }
        });
        new Ext.get('displayRangeSubTpl').setHTML('');
        new Ext.get('frequencySubTpl').setHTML('');
        form.doLayout();
    },

    onGroupChange: function (combo, newValue) {
        var me = this,
            form = me.getDataCollectionKpiEditForm(),
            collectionTypeValue = form.down('#cmb-collectionType').getValue(),
            frequency = form.down('#cmb-frequency'),
            displayRange = form.down('#cmb-display-range'),
            afterSubTpl = '';

        if (newValue) {
            var name = typeof combo.getValue() == 'object' ? combo.getValue().name : combo.getRawValue();

            afterSubTplTxt = '<div class="x-form-display-field"><i>' + Uni.I18n.translate('datacollectionkpis.templateTxt', 'MDC', "This change will be also applied for '{0}' {1} task", [name,
                    collectionTypeValue == 'connection' ? Uni.I18n.translate('datacollectionkpis.communicationKPI', 'MDC', 'communication KPI') :
                        Uni.I18n.translate('datacollectionkpis.connectionKPI', 'MDC', 'connection KPI')]) + '</i></div>';
        }

        new Ext.get('displayRangeSubTpl').setHTML(afterSubTplTxt);
        new Ext.get('frequencySubTpl').setHTML(afterSubTplTxt);
        form.doLayout();
    }
});
