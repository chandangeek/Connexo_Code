/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.controller.TaskBulk', {
    extend: 'Ext.app.Controller',
    views: [
        'Bpm.view.task.bulk.Browse'
    ],
    refs: [
        {
            ref: 'wizard',
            selector: 'tasks-bulk-browse #tasks-bulk-wizard'
        },
        {
            ref: 'navigation',
            selector: 'tasks-bulk-browse #tasks-bulk-navigation'
        },
        {
            ref: 'groupGrid',
            selector: 'tasks-bulk-browse #tasks-complete-grid'
        },
        {
            ref: 'taskExecutionContent',
            selector: 'tasks-bulk-browse task-complete-form'
        }
    ],

    stores: [
        'Bpm.store.task.Tasks',
        'Bpm.store.task.TasksBuffered',
        'Bpm.store.task.TasksUsers',
        'Bpm.store.task.TaskGroups',
        'Bpm.store.task.TasksFilterAllUsers'
    ],
    models: [
        'Bpm.model.task.TaskForm'
    ],
    config: {
        manageTaskActions: []
    },
    listeners: {
        retryRequest: function (wizard, failedItems) {
        }
    },

    alltasksBulk: [],

    init: function () {
        this.control({
            'tasks-bulk-browse tasks-bulk-wizard button[action=step-next]': {
                click: this.moveTo
            },
            'tasks-bulk-browse tasks-bulk-wizard button[action=step-back]': {
                click: this.moveTo
            },
            'tasks-bulk-browse tasks-bulk-wizard button[action=confirm-action]': {
                click: this.moveTo
            },
            'tasks-bulk-browse tasks-bulk-wizard button[action=finish]': {
                click: this.cancelFinishWizard
            },
            'tasks-bulk-browse tasks-bulk-wizard button[action=cancel]': {
                click: this.cancelFinishWizard
            },
            'tasks-bulk-browse #tasks-bulk-navigation': {
                movetostep: this.moveTo
            },
            'tasks-bulk-browse #tasks-complete-grid': {
                beforedeselect: this.beforeDeselect,
                select: this.showGroupPreview
            }

        });
    },

    checkForSelectedForm: function(nextStep){
        var me = this,
            tasksGroupGrid = me.getGroupGrid(),
            selectionModel = tasksGroupGrid.getSelectionModel(),
            selectedRecord = selectionModel.getSelection(),
            taskExecutionContent = me.getTaskExecutionContent(),
            propertyForm = taskExecutionContent.down('property-form');

        propertyForm.updateRecord();

        if (propertyForm.getRecord()) {
            selectedRecord[0].tasksForm.beginEdit();
            selectedRecord[0].tasksForm.propertiesStore = propertyForm.getRecord().properties();
            selectedRecord[0].tasksForm.endEdit();
        }
        selectedRecord[0].tasksForm.beginEdit();
        selectedRecord[0].tasksForm.set('action', 'saveTask');
        selectedRecord[0].tasksForm.set('id', selectedRecord[0].data.taskIds[0]);
        selectedRecord[0].tasksForm.endEdit();
        selectedRecord[0].tasksForm.save({
            success: function () {
                selectedRecord[0].beginEdit();
                selectedRecord[0].set('hasMandatory', false);
                selectedRecord[0].data.tasksForm = this.data;
                selectedRecord[0].endEdit();
                me.loadJbpmForm(selectedRecord[0]);
                me.getWizard().setLoading(false);
                if (!me.validateCurrentStep(nextStep - 1)) {
                    Ext.resumeLayouts(true);
                    return;
                }
                me.prepareNextStep(nextStep);
                me.getWizard().getLayout().setActiveItem(nextStep - 1);
                me.getNavigation().moveToStep(nextStep);
                Ext.resumeLayouts(true);
            },
            failure: function (response, operation) {

                selectedRecord[0].beginEdit();
                selectedRecord[0].set('hasMandatory', true);
                selectedRecord[0].data.tasksForm = this.data;
                selectedRecord[0].endEdit();
                me.loadJbpmForm(selectedRecord[0]);
                me.getWizard().setLoading(false);
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        propertyForm.getForm().markInvalid('');
                        propertyForm.getForm().markInvalid(json.errors);
                    }
                }
                if (!me.validateCurrentStep(nextStep - 1)) {
                    Ext.resumeLayouts(true);
                    return;
                }
            }
        });
    },


    requestFormValidation: function (propertyForm, record, loadBpmForm) {
        var me = this;

        propertyForm.updateRecord();

        if (propertyForm.getRecord()) {
            record.tasksForm.beginEdit();
            record.tasksForm.propertiesStore = propertyForm.getRecord().properties();
            record.tasksForm.endEdit();
        }

        record.tasksForm.beginEdit();
        record.tasksForm.set('action', 'saveTask');
        record.tasksForm.set('id', record.data.taskIds[0]);
        record.tasksForm.endEdit();
        record.tasksForm.save({
            success: function () {
                record.beginEdit();
                record.set('hasMandatory', false);
                record.data.tasksForm = this.data;
                record.mandatoryValidated = true;
                record.endEdit();
                if(loadBpmForm) me.loadJbpmForm(record);
            },
            failure: function (response, operation) {
                if (operation.response.status == 400) {
                    record.beginEdit();
                    record.set('hasMandatory', true);
                    record.data.tasksForm = this.data;
                    if (loadBpmForm) {
                        record.mandatoryValidated = false;
                    }
                    record.endEdit();

                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        propertyForm.getForm().markInvalid('');
                        propertyForm.getForm().markInvalid(json.errors);
                    }
                    me.getWizard().setLoading(false);
                }
            }
        });
    },

    validateMandatoryFieldsInForm: function (propertyForm, record) {
        var me = this;

        me.requestFormValidation(propertyForm, record, true);
        return;

    },

    beforeDeselect: function (grid, record) {

        var me = this,
            taskExecutionContent = me.getTaskExecutionContent(),
            propertyForm = taskExecutionContent.down('property-form');

        me.requestFormValidation(propertyForm, record, false);
        return true;

    },

    showGroupPreview: function (selectionModel, record) {
        var me = this,
            wizard = me.getWizard(),
            preview = wizard.down('bpm-task-group-preview'),
            propertyForm = wizard.down('property-form');

        wizard.setLoading();
        if(record.get('name') != 'null')
            preview.setTitle(record.get('name'));
        else
            preview.setTitle('');

        me.loadJbpmForm(record);

        if(record.mandatoryValidated == false)
        {
            me.getWizard().setLoading();
            me.validateMandatoryFieldsInForm(propertyForm, record);
        }


    },

    loadJbpmForm: function (taskRecord) {
        var me = this,
            taskExecutionContent = me.getTaskExecutionContent(),
            propertyForm;

        propertyForm = taskExecutionContent.down('property-form');

            taskExecutionContent.setLoading();

            if (taskRecord && taskRecord.tasksForm && taskRecord.tasksForm.properties() && taskRecord.tasksForm.properties().count()) {
                propertyForm.loadRecord(taskRecord.tasksForm);
                propertyForm.show();
                me.getWizard().setLoading(false);
            } else {
                propertyForm.loadRecord(taskRecord.tasksForm);
                propertyForm.add(
                    {
                        xtype: 'component',
                        html: Uni.I18n.translate('bpm.task.bulk.NoTaskForm', 'BPM', 'No form defined for this group of tasks.'),
                        style: {
                            fontStyle: 'italic',
                            color: '#999'
                        }
                    });
                me.getWizard().setLoading(false);
            }

        taskExecutionContent.setLoading(false);

    },

    showOverview: function () {
        var me = this,
            taskTasksBuffered = me.getStore('Bpm.store.task.TasksBuffered'),
            filteredTasks = Ext.create('Bpm.store.task.Tasks'),
            router = this.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            tasksRoute = router.getRoute('workspace/tasks'),
            tasksGroupsStore = me.getStore('Bpm.store.task.TaskGroups'),
            sort, params = [],
            tasks = [],
            queryParams = {};

        sort = router.arguments.sort;
        user = router.arguments.user;
        dueDate = router.arguments.dueDate;
        taskStatus = router.arguments.status;
        process = router.arguments.process;

        tasksRoute.params.sort = tasksRoute.params.user = tasksRoute.params.dueDate = tasksRoute.params.status = tasksRoute.params.process = undefined;
        tasksRoute.params.use = true;

        sort && (sort != '') && (queryParams.sort = tasksRoute.params.sort = sort);
        user && (user != '') && (queryParams.user = tasksRoute.params.user = user) && (params.push({
            property: 'user',
            value: Array.isArray(user) ? user : [user]
        }));
        dueDate && (dueDate != '') && (queryParams.dueDate = tasksRoute.params.dueDate = dueDate) && (params.push({
            property: 'dueDate',
            value: Array.isArray(dueDate) ? dueDate : [dueDate]
        }));
        taskStatus && (taskStatus != '') && (queryParams.status = tasksRoute.params.status = taskStatus) && (params.push({
            property: 'status',
            value: Array.isArray(taskStatus) ? taskStatus : [taskStatus]
        }));
        process && (process != '') && (queryParams.process = tasksRoute.params.process = process) && (params.push({
            property: 'process',
            value: Array.isArray(process) ? process : [process]
        }));

        queryString.sort = sort;
        window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));

        filteredTasks.getProxy().pageParam = undefined;
        filteredTasks.getProxy().startParam = undefined;
        filteredTasks.getProxy().limitParam = undefined;
        filteredTasks.filter(params);
        filteredTasks.load({
            callback: function (records, operation, success) {
                if (success) {
                    me.getApplication().fireEvent('changecontentevent', Ext.widget('tasks-bulk-browse', {
                        router: me.getController('Uni.controller.history.Router')
                    }));

                    Ext.Array.each(filteredTasks.data.items, function (item) {
                        tasks.push(
                            item.getId()
                        );
                    });

                    me.alltasksBulk = tasks;
                    taskTasksBuffered.data.clear();
                    tasksGroupsStore.data.clear();
                    taskTasksBuffered.loadPage(1);
                }

            }
        });

    },

    loadTaskGroups: function () {
        var me = this,
            wizard = me.getWizard(),
            selectionGrid = wizard.down('bulk-selection-grid'),
            taskGroupItems = [],
            qParams,
            tasksGroupsStore = me.getStore('Bpm.store.task.TaskGroups');


        if (tasksGroupsStore.data.items.length != 0) return;
        me.getWizard().setLoading();

        if (selectionGrid.isAllSelected()) {
            taskGroupItems = me.alltasksBulk;
        }
        else {
            Ext.Array.each(selectionGrid.getSelectionModel().getSelection(), function (item) {
                taskGroupItems.push(
                    item.getId()
                )
            });
        }
        qParams = {
            taskGroups: [{taskIds: taskGroupItems}]
        };

        Ext.Ajax.request({
            url: '/api/bpm/runtime/tasks/mandatory',
            jsonData: qParams,
            method: 'POST',
            success: function (response) {
                tasksGroupsStore.loadRawData(Ext.decode(response.responseText), true);
                me.getGroupGrid().getSelectionModel().select(0);
                me.getWizard().setLoading(false);
            },
            failure: function (response) {
                console.log(response);
                me.getWizard().setLoading(false);
            }
        });
    },

    doRequest: function () {
        var me = this,
            wizard = me.getWizard(),
            selectionGrid = wizard.down('bulk-selection-grid'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            action = wizard.down('#tasks-bulk-action-radiogroup').getValue().action,
            tasksGroupGrid = me.getGroupGrid(),
            manageTaskForm,
            tasksQueryParams ={},
            tasksPayload,
            groupStore,
            tasks = [];

        manageTaskForm = wizard.down('#tskbw-step3').down('task-manage-form');
        if (selectionGrid.isAllSelected()) {
            tasks = me.alltasksBulk;
        }
        else {
            Ext.Array.each(selectionGrid.getSelectionModel().getSelection(), function (item) {
                tasks.push(
                    item.getId()
                );
            });
        }

        if (action === 'taskmanagement') {
            tasksPayload = {
                taskGroups: [{taskIds: tasks}]
            };
            Ext.each(me.manageTaskActions, function (item) {
                switch (item) {
                    case 'userAssign':
                        tasksQueryParams.assign = manageTaskForm.down('combo[itemId=cbo-user-assignee]').getRawValue();
                        break;
                    case 'workgroupAssign':
                        tasksQueryParams.workgroup = manageTaskForm.down('combo[itemId=cbo-workgroup-assignee]').getRawValue();
                        break;
                    case 'setDueDate':
                        tasksQueryParams.setDueDate = moment(manageTaskForm.down('#task-due-date').getValue()).valueOf();
                        break;
                    case 'setPriority':
                        tasksQueryParams.setPriority = manageTaskForm.down('#num-priority-number').getValue();
                        break;
                }

            });
        }
        else {

            wizard.setLoading(true);
            groupStore = tasksGroupGrid.getStore();
            groupStore.each(function(record) {
                for (var i = 0; i < record.get('tasksForm').properties.length; i++) {

                    if (record.tasksForm.propertiesStore.data.items[i].getPropertyValue().get('value')) {
                        if (typeof record.get('tasksForm').properties[i].propertyValueInfo == 'undefined' || record.get('tasksForm').properties[i].propertyValueInfo === null) {
                            var propertyValueInfoTest = Ext.create('Uni.property.model.PropertyValue');
                            record.get('tasksForm').properties[i].propertyValueInfo = propertyValueInfoTest.data;
                        }
                        record.get('tasksForm').properties[i].propertyValueInfo.value = record.tasksForm.propertiesStore.data.items[i].getPropertyValue().get('value');

                    }
                    else
                    if (record.tasksForm.propertiesStore.data.items[i].getPropertyType().get('simplePropertyType') == 'BOOLEAN') {
                        if (typeof record.get('tasksForm').properties[i].propertyValueInfo == 'undefined' || record.get('tasksForm').properties[i].propertyValueInfo === null) {
                            var propertyValueInfoTest = Ext.create('Uni.property.model.PropertyValue');
                            record.get('tasksForm').properties[i].propertyValueInfo = propertyValueInfoTest.data;
                        }
                        record.get('tasksForm').properties[i].propertyValueInfo.value = false;
                    }

                }


            });

            tasksPayload = {
                taskGroups: Ext.pluck(me.getStore('Bpm.store.task.TaskGroups').data.items, 'data')
            };

        }

        wizard.setLoading(true);

        wizard.down('#tskbw-step5').showProgressBar(action);

        Ext.Ajax.request({
            url: '/api/bpm/runtime/managetasks',
            method: 'POST',
            jsonData: tasksPayload,
            params: tasksQueryParams,
            success: function (response) {
                wizard.down('#tskbw-step5').setResultMessage(action, true, Ext.decode(response.responseText).total);
                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                wizard.setLoading(false);
            },
            failure: function (response, operation) {
                if (operation.response.status === 400) {
                    wizard.down('#tskbw-step5').setResultMessage(action, false, Ext.decode(response.responseText).total, Ext.decode(response.responseText).failed);
                    window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                    wizard.setLoading(false);
                }
            }
        });
    },

    moveTo: function (button) {
        var me = this,
            wizardLayout = me.getWizard().getLayout(),
            currentStep = parseInt(wizardLayout.getActiveItem().getItemId().replace('tskbw-step', '')),
            direction,
            nextStep;

        if (button.action === 'step-next' || button.action === 'confirm-action') {
            direction = 1;
            nextStep = currentStep + direction;
        } else {
            direction = -1;

            if (button.action === 'step-back') {
                nextStep = currentStep + direction;
            } else {
                nextStep = button;
            }
        }

        Ext.suspendLayouts();

        if (direction > 0) {

            if(me.getWizard().down('#tasks-bulk-action-radiogroup').getValue().action === 'taskexecute'&& currentStep === 3 && direction === 1) {
                me.getWizard().setLoading(true);
                me.checkForSelectedForm(nextStep);
            }
            else
            {
                if (!me.validateCurrentStep(currentStep)) {
                    Ext.resumeLayouts(true);
                    return
                }
                me.prepareNextStep(nextStep);
                wizardLayout.setActiveItem(nextStep - 1);
                me.getNavigation().moveToStep(nextStep);
                Ext.resumeLayouts(true);
            }

        }
        else {
            me.prepareNextStep(nextStep);
            wizardLayout.setActiveItem(nextStep - 1);
            me.getNavigation().moveToStep(nextStep);
            Ext.resumeLayouts(true);
        }

    },

    validateCurrentStep: function (stepNumber) {
        var me = this,
            valid = true,
            stepView,
            selectionGrid,
            manageTaskForm,
            workgroupAssignee, userAssignee,
            dueDateContainer,
            priorityContainer,
            tasksGroupsStore = me.getStore('Bpm.store.task.TaskGroups'),
            assigneeCombo;

        switch (stepNumber) {
            case 1:
                stepView = me.getWizard().down('#tskbw-step1');
                selectionGrid = stepView.down('bulk-selection-grid');
                valid = !(!selectionGrid.isAllSelected() && !selectionGrid.getSelectionModel().getSelection().length);
                stepView.down('#step1-error-message').setVisible(!valid);
                stepView.down('#selection-grid-error').setVisible(!valid);
                break;
            case 2:
                stepView = me.getWizard().down('#tskbw-step2');

                if (me.getWizard().down('#step3-error-message')) {
                    me.getWizard().down('#step3-error-message').setVisible(false);
                    me.getWizard().down('#controls-selection-error').setVisible(false);
                    me.getWizard().down('#mandatory-fields-error').setVisible(!valid);
                }
                break;
            case 3:
                stepView = me.getWizard().down('#tskbw-step3');
                if (me.getWizard().down('#tasks-bulk-action-radiogroup').getValue().action === 'taskmanagement') {

                    manageTaskForm = stepView.down('task-manage-form');
                    workgroupAssignee = manageTaskForm.down('combobox[itemId=cbo-workgroup-assignee]');
                    userAssignee = manageTaskForm.down('combobox[itemId=cbo-user-assignee]');
                    dueDateContainer = manageTaskForm.down('fieldcontainer[name=setDueDate]');
                    priorityContainer = manageTaskForm.down('fieldcontainer[name=setPriority]');

                    if (workgroupAssignee.disabled && userAssignee.disabled && dueDateContainer.disabled && priorityContainer.disabled) {
                        valid = false;
                        stepView.down('#controls-selection-error').setVisible(!valid);
                        stepView.down('#step3-error-message').setVisible(!valid);
                        break;
                    }
                    else {
                        stepView.down('#controls-selection-error').setVisible(!valid);
                    }
                    if (!workgroupAssignee.disabled && workgroupAssignee.getRawValue() === "") {
                        valid = false;
                    }

                    if (!userAssignee.disabled && userAssignee.getRawValue() === "") {
                        valid = false;
                    }

                }
                else {
                    var taskWithMandatoryField = tasksGroupsStore.findRecord('hasMandatory', true);
                    if (taskWithMandatoryField) {
                        tasksGroupsStore.each(function(record) {
                            if (record.get('hasMandatory') === true) {
                                record.mandatoryValidated = false;
                            }
                        });
                        valid = false;
                    }

                    stepView.down('#step3-error-message').setVisible(!valid);
                    stepView.down('#mandatory-fields-error').setVisible(!valid);
                }

                if (stepView.down('#user-selection-error'))
                    stepView.down('#user-selection-error').setVisible(!valid);
                stepView.down('#step3-error-message').setVisible(!valid);
                me.getWizard().setLoading(false);
                break;
        }

        return valid;
    },
    prepareNextStep: function (stepNumber) {
        var me = this,
            wizard = me.getWizard(),
            buttons = wizard.getDockedComponent('tskbw-buttons'),
            nextBtn = buttons.down('[action=step-next]'),
            backBtn = buttons.down('[action=step-back]'),
            confirmBtn = buttons.down('[action=confirm-action]'),
            finishBtn = buttons.down('[action=finish]'),
            cancelBtn = buttons.down('[action=cancel]');

        switch (stepNumber) {
            case 1:
                me.getStore('Bpm.store.task.TaskGroups').data.clear();
                nextBtn.show();
                backBtn.show();
                backBtn.disable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 2:
                wizard.down('#tskbw-step4').removeAll(true);
                me.loadTaskGroups();
                nextBtn.show();
                nextBtn.enable();
                backBtn.show();
                backBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 3:
                wizard.down('#tskbw-step4').removeAll(true);
                me.getWizard().down('#tskbw-step3').setForms(wizard.down('#tasks-bulk-action-radiogroup').getValue().action);
                me.nextBtnCounter = 0;

                nextBtn.show();
                backBtn.show();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 4:
                me.manageTaskActions = me.getWizard().down('#tskbw-step3').getManagementActions();
                wizard.down('#tskbw-step4').setConfirmationMessage(wizard.down('#tasks-bulk-action-radiogroup').getValue().action);
                nextBtn.hide();
                backBtn.show();
                backBtn.enable();
                confirmBtn.show();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 5:
                me.doRequest();
                me.getNavigation().jumpBack = false;
                nextBtn.hide();
                backBtn.hide();
                backBtn.enable();
                confirmBtn.hide();
                finishBtn.show();
                cancelBtn.hide();
                break;
        }
    },
    cancelFinishWizard: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router');
        router.getRoute('workspace/tasks').forward(null, router.arguments);

    }
});