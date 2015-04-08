Ext.define('Dsh.controller.ConnectionsBulk', {
    extend: 'Ext.app.Controller',

    stores: [
        'Dsh.store.ConnectionTasksBuffered'
    ],

    views: [
        'Dsh.view.connectionsbulk.Browse'
    ],

    refs: [
        {
            ref: 'wizard',
            selector: 'connections-bulk-browse #connections-bulk-wizard'
        },
        {
            ref: 'navigation',
            selector: 'connections-bulk-browse #connections-bulk-navigation'
        }
    ],

    init: function () {
        this.control({
            'connections-bulk-browse connections-bulk-wizard button[action=step-next]': {
                click: this.moveTo
            },
            'connections-bulk-browse connections-bulk-wizard button[action=step-back]': {
                click: this.moveTo
            },
            'connections-bulk-browse connections-bulk-wizard button[action=confirm-action]': {
                click: this.moveTo
            },
            'connections-bulk-browse #connections-bulk-navigation': {
                movetostep: this.moveTo
            }
        });
    },

    showOverview: function () {
        var me = this,
            connectionTasksBufferedStore = me.getStore('Dsh.store.ConnectionTasksBuffered');

        this.getApplication().fireEvent('changecontentevent', Ext.widget('connections-bulk-browse', {
            router: me.getController('Uni.controller.history.Router')
        }));
        connectionTasksBufferedStore.data.clear();
        connectionTasksBufferedStore.loadPage(1);
    },

    doRequest: function () {
        var me = this,
            wizard = me.getWizard(),
            selectionGrid = wizard.down('bulk-selection-grid'),
            action = wizard.down('#connections-bulk-action-radiogroup').getValue().action,
            data = {},
            url;

        if (selectionGrid.isAllSelected()) {
            data.filter = {};
            me.getStore('Dsh.store.ConnectionTasksBuffered').filters.each(function (item) {
                data.filter[item.property] = item.value;
            });
        } else {
            data.connections = [];
            Ext.Array.each(selectionGrid.getSelectionModel().getSelection(), function (item) {
                data.connections.push(item.getId());
            });
        }

        switch (action) {
            case 'runNow':
                url = '/api/dsr/connections/run';
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
                    wizard.down('#cnbw-step4').setResultMessage(action, success);
                }
            }
        });
    },

    moveTo: function (button) {
        var me = this,
            wizardLayout = me.getWizard().getLayout(),
            currentStep = parseInt(wizardLayout.getActiveItem().getItemId().replace('cnbw-step', '')),
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
                step1View = me.getWizard().down('#cnbw-step1');
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
            buttons = wizard.getDockedComponent('cnbw-buttons'),
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
                backBtn.show();
                backBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 3:
                wizard.down('#cnbw-step3').setConfirmationMessage(wizard.down('#connections-bulk-action-radiogroup').getValue().action);
                nextBtn.hide();
                backBtn.show();
                backBtn.enable();
                confirmBtn.show();
                finishBtn.hide();
                cancelBtn.show();
                break;
            case 4:
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