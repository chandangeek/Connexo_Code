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
        'Bpm.store.task.TasksUsers'
    ],

    listeners: {
        retryRequest: function (wizard, failedItems) {
            this.setFailedBulkRecordIssues(failedItems);
            this.onWizardFinishedEvent(wizard);
        }
    },

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
            'tasks-bulk-browse #tasks-bulk-navigation': {
                movetostep: this.moveTo
            }
        });
    },

    showOverview: function () {
        var me = this,
            taskTasksBuffered = me.getStore('Bpm.store.task.Tasks');

        this.getApplication().fireEvent('changecontentevent', Ext.widget('tasks-bulk-browse', {
            router: me.getController('Uni.controller.history.Router')
        }));
        taskTasksBuffered.data.clear();
        taskTasksBuffered.loadPage(1);
    },

    doRequest: function () {
        var me = this,
            wizard = me.getWizard(),
            selectionGrid = wizard.down('bulk-selection-grid'),
            action = wizard.down('#tasks-bulk-action-radiogroup').getValue().action,
            data = {},
            url;

        if (selectionGrid.isAllSelected()) {
            data.filter = {};
            me.getStore('Bpm.store.Tasks').filters.each(function (item) {
                data.filter[item.property] = item.value;
            });
        } else {
            data.tasks = [];
            Ext.Array.each(selectionGrid.getSelectionModel().getSelection(), function (item) {
                data.tasks.push(item.getId());
            });
        }

        switch (action) {
            case 'run':
                url = '/api/dsr/communications/run';
                break;
            case 'runNow':
                url = '/api/dsr/communications/runnow';
                break;
        }

        wizard.setLoading(true);

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: data,
            callback: function (options, success) {
                if (wizard.rendered) {
                    wizard.setLoading(false);
                    wizard.down('#tskbw-step4').setResultMessage(action, success);
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
            step1View,
            selectionGrid;

        switch (stepNumber) {
            case 1:
                step1View = me.getWizard().down('#tskbw-step1');
                selectionGrid = step1View.down('bulk-selection-grid');
                valid = !(!selectionGrid.isAllSelected() && !selectionGrid.getSelectionModel().getSelection().length);
                step1View.down('#step1-error-message').setVisible(!valid);
                step1View.down('#selection-grid-error').setVisible(!valid);
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
                nextBtn.show();
                nextBtn.enable();
                backBtn.show();
                backBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 3:
                //wizard.down('#tskbw-step3').setConnectionType(me.getConnectionType());
                //wizard.down('#tskbw-step3').setProperties(me.getProperties());
                //me.passOnEditFunc();
                me.nextBtnCounter = 0;
                nextBtn.show();
                nextBtn.disable();
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
    }
});