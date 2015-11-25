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
        }
    ],

    stores: [
        'Bpm.store.task.Tasks',
        'Bpm.store.task.TasksBuffered',
        'Bpm.store.task.TasksUsers'
    ],
    config: {
        manageTaskActions: null
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
            }
        });
    },

    showOverview: function () {
        var me = this,
            taskTasksBuffered = me.getStore('Bpm.store.task.TasksBuffered'),
            filteredTasks = me.getStore('Bpm.store.task.Tasks'),
            router = this.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            tasksRoute = router.getRoute('workspace/taksmanagementtasks'),
            sort,
            tasks = [];

        this.getApplication().fireEvent('changecontentevent', Ext.widget('tasks-bulk-browse', {
            router: me.getController('Uni.controller.history.Router')
        }));

        Ext.Array.each(filteredTasks.data.items, function (item) {
            tasks.push({
                id: item.getId()
            });
        });

        me.alltasksBulk = tasks;
        taskTasksBuffered.data.clear();
        taskTasksBuffered.loadPage(1);
    },

    doRequest: function () {
        var me = this,
            wizard = me.getWizard(),
            selectionGrid = wizard.down('bulk-selection-grid'),
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            action = wizard.down('#tasks-bulk-action-radiogroup').getValue().action,
            manageTaskForm,
            operation,
            tasks = [],
            url;

        manageTaskForm = wizard.down('#tskbw-step3').down('task-manage-form');
        if (selectionGrid.isAllSelected()) {
            tasks = me.alltasksBulk;
            }
        else {
            Ext.Array.each(selectionGrid.getSelectionModel().getSelection(), function (item) {
                tasks.push({
                    id: item.getId()
                });
            });

        }
        operation = {
            tasks: Ext.encode(tasks)
        };

        Ext.each(manageTaskActions, function (item) {
            switch(item)
            {
                case 'assign':
                    operation.assign = manageTaskForm.down('combobox[name=assigneeCombo]').getRawValue();
                    break;
                case 'setDueDate':
                    operation.setDueDate = moment(manageTaskForm.down('#task-due-date').getValue()).valueOf();
                    break;
                case 'setPriority':
                    operation.setPriority = manageTaskForm.down('#num-priority-number').getValue();
                    break;
            }

        });

        url = '/api/bpm/runtime/managetasks';

        wizard.setLoading(true);

        wizard.down('#tskbw-step5').showProgressBar(action);

        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: operation,
            params:operation,

            success: function (option) {

                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
            },
            callback: function (options, success) {
                if (wizard.rendered) {
                    window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                    wizard.down('#tskbw-step5').setResultMessage(action, success);
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
            if(currentStep === 2 && me.getWizard().down('#tasks-bulk-action-radiogroup').getValue().action === 'taskexecute')
                direction = 2;
            else
                direction = 1;
            nextStep = currentStep + direction;
        } else {
            if(currentStep === 4 && me.getWizard().down('#tasks-bulk-action-radiogroup').getValue().action === 'taskexecute')
                direction = -2;
            else
                direction = -1;
            if (button.action === 'step-back') {
                nextStep = currentStep + direction;
            } else {
                nextStep = button;
            }
        }

        Ext.suspendLayouts();

        if (direction > 0) {
            if (!me.validateCurrentStep(currentStep)) {
                Ext.resumeLayouts(true);
                return
            }
        }

        me.prepareNextStep(nextStep);
        wizardLayout.setActiveItem(nextStep - 1);
        me.getNavigation().moveToStep(nextStep);
        Ext.resumeLayouts(true);
    },

    validateCurrentStep: function (stepNumber) {
        var me = this,
            valid = true,
            stepView,
            selectionGrid,
            manageTaskForm,
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
                manageTaskActions = me.getWizard().down('#tskbw-step2').getManagementActions();
                valid = ((manageTaskActions.length > 0 && me.getWizard().down('#tasks-bulk-action-radiogroup').getValue().action === 'taskmanagement')
                    || me.getWizard().down('#tasks-bulk-action-radiogroup').getValue().action === 'taskexecute');

                stepView.down('#step2-error-message').setVisible(!valid);
                stepView.down('#action-selection-error').setVisible(!valid);
                break;
            case 3:
                stepView = me.getWizard().down('#tskbw-step3');
                manageTaskForm = stepView.down('task-manage-form');
                assigneeCombo = manageTaskForm.down('combobox[name=assigneeCombo]');

                Ext.each(manageTaskActions, function (item) {
                    switch(item)
                    {
                        case 'assign':
                            if(assigneeCombo.getRawValue() === "")
                            {
                                valid = false;
                            }
                            break;
                    }
                });

                stepView.down('#user-selection-error').setVisible(!valid);
                stepView.down('#step3-error-message').setVisible(!valid);
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
                nextBtn.show();
                backBtn.show();
                backBtn.disable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 2:
                wizard.down('#tskbw-step4').removeAll(true);
                wizard.down('#tskbw-step3').resetControls();
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
                me.setManageTaskActions(wizard.down('#tskbw-step2').getManagementActions());
                wizard.down('#tskbw-step3').setControls(me.getManageTaskActions());
                me.nextBtnCounter = 0;
                nextBtn.show();
                backBtn.show();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 4:
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
        var me= this,
            router = this.getController('Uni.controller.history.Router');
        router.getRoute('workspace/taksmanagementtasks').forward(null, router.arguments);

    }
});