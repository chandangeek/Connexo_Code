/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.controller.CommunicationsBulk', {
    extend: 'Ext.app.Controller',

    stores: [
        'Dsh.store.CommunicationTasksBuffered'
    ],

    views: [
        'Dsh.view.communicationsbulk.Browse'
    ],

    refs: [
        {
            ref: 'wizard',
            selector: 'communications-bulk-browse #communications-bulk-wizard'
        },
        {
            ref: 'navigation',
            selector: 'communications-bulk-browse #communications-bulk-navigation'
        }
    ],

    init: function () {
        this.control({
            'communications-bulk-browse communications-bulk-wizard button[action=step-next]': {
                click: this.moveTo
            },
            'communications-bulk-browse communications-bulk-wizard button[action=step-back]': {
                click: this.moveTo
            },
            'communications-bulk-browse communications-bulk-wizard button[action=confirm-action]': {
                click: this.moveTo
            },
            'communications-bulk-browse #communications-bulk-navigation': {
                movetostep: this.moveTo
            }
        });
    },

    showOverview: function () {
        var me = this;

        this.getApplication().fireEvent('changecontentevent', Ext.widget('communications-bulk-browse', {
            router: me.getController('Uni.controller.history.Router')
        }));
    },

    doRequest: function () {
        var me = this,
            wizard = me.getWizard(),
            selectionGrid = wizard.down('bulk-selection-grid'),
            action = wizard.down('#communications-bulk-action-radiogroup').getValue().action,
            data = {},
            url;

        if (selectionGrid.isAllSelected()) {
            var filterItems = me.getFilterObjectFromQueryString();
            data.filter = {};
            for (var dataIndex in filterItems) {
                var value = filterItems[dataIndex];
                if (filterItems.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                    data.filter[dataIndex] = value;
                }
            }
        } else {
            data.communications = [];
            Ext.Array.each(selectionGrid.getSelectionModel().getSelection(), function (item) {
                data.communications.push(item.getId());
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
                    wizard.down('#cmbw-step4').setResultMessage(action, success);
                }
            }
        });
    },

    moveTo: function (button) {
        var me = this,
            wizardLayout = me.getWizard().getLayout(),
            currentStep = parseInt(wizardLayout.getActiveItem().getItemId().replace('cmbw-step', '')),
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
                step1View = me.getWizard().down('#cmbw-step1');
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
            buttons = wizard.getDockedComponent('cmbw-buttons'),
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
                wizard.down('#cmbw-step3').setConfirmationMessage(wizard.down('#communications-bulk-action-radiogroup').getValue().action);
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
    },

    getFilterObjectFromQueryString: function() {
        var filterObject = Uni.util.QueryString.getQueryStringValues(false);
        this.adaptFilterObject(filterObject);
        return filterObject;
    },

    adaptFilterObject: function(filterObject) {
        // Assure that properties that are expected to be an int array, are indeed int arrays

        var props = ['deviceTypes', 'deviceGroups', 'comTasks', 'comSchedules'];
        Ext.Array.each(props, function(prop) {
            if (filterObject.hasOwnProperty(prop)) {
                if (Ext.isArray(filterObject[prop])) {
                    for (i = 0; i < filterObject[prop].length; i++) {
                        filterObject[prop][i] = parseInt(filterObject[prop][i]);
                    }
                } else {
                    var theOneValue = filterObject[prop];
                    filterObject[prop] = [];
                    filterObject[prop][0] = !Ext.isNumber(theOneValue) ? parseInt(theOneValue) : theOneValue;
                }
            }
        });

        Ext.Array.each(['currentStates'], function(prop) {
            if (filterObject.hasOwnProperty(prop)) {
                if (!Ext.isArray(filterObject[prop])) {
                    filterObject[prop] = [filterObject[prop]];
                }
            }
        });
    }

});