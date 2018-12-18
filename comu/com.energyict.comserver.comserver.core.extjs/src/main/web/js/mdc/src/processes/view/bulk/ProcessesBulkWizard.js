/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.ProcessesBulkWizard', {
    //extend: 'Isu.view.issues.bulk.Wizard',
    extend: 'Mdc.processes.view.bulk.Wizard',
    alias: 'widget.process-bulk-wizard',
    itemId: 'process-bulk-wizard',
    titlePrefix: Uni.I18n.translate('general.title.bulkActions', 'ISU', 'Bulk action'),
    includeSubTitle: true,

    requires: [
        //'Isu.view.issues.bulk.Step1',
        'Mdc.processes.view.bulk.Step1',
        'Mdc.processes.view.bulk.Step2',
        'Mdc.processes.view.bulk.Step3',
        'Mdc.processes.view.bulk.Step4',
        'Mdc.processes.view.bulk.Step5'
        /*'Isu.view.issues.bulk.Step2',
        'Isu.view.issues.bulk.Step3',
        'Isu.view.issues.bulk.Step4',
        'Isu.view.issues.bulk.Step5'*/
    ],

    title: 'Wizard',
    ui: 'large',

    items: [
        {
            itemId: 'processes-bulk-step1',
            //xtype: 'bulk-step1',
            xtype: 'processes-bulk-step1',
            buttonsConfig: {
                prevbuttonDisabled: true,
                nextbuttonDisabled: false,
                nextActionPreparationButtonDisabled: true,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: true,
                finishSuccesfullButtonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: true,
                nextActionPreparationButtonVisible: false,
                cancelbuttonVisible: true,
                confirmbuttonVisible: false,
                finishSuccesfullButtonVisible: false
            }
        },
        {
            itemId: 'processes-bulk-step2',
            xtype: 'processes-bulk-step2',
            buttonsConfig: {
                prevbuttonDisabled: false,
                nextbuttonDisabled: false,
                nextActionPreparationButtonDisabled: true,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: true,
                finishSuccesfullButtonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: true,
                nextActionPreparationButtonVisible: false,
                cancelbuttonVisible: true,
                confirmbuttonVisible: false,
                finishSuccesfullButtonVisible: false
            }
        },
        {
            itemId: 'processes-bulk-step3',
            xtype: 'processes-bulk-step3',
            buttonsConfig: {
                prevbuttonDisabled: false,
                nextbuttonDisabled: true,
                nextActionPreparationButtonDisabled: false,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: true,
                finishSuccesfullButtonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: false,
                nextActionPreparationButtonVisible: true,
                cancelbuttonVisible: true,
                confirmbuttonVisible: false,
                finishSuccesfullButtonVisible: false
            }
        },
        {
            itemId: 'processes-bulk-step4',
            xtype: 'processes-bulk-step4',
            buttonsConfig: {
                prevbuttonDisabled: false,
                nextbuttonDisabled: true,
                nextActionPreparationButtonDisabled: true,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: false,
                finishSuccesfullButtonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: false,
                nextActionPreparationButtonVisible: false,
                cancelbuttonVisible: true,
                confirmbuttonVisible: true,
                finishSuccesfullButtonVisible: false
            }
        },
        {
            itemId: 'processes-bulk-step5',
            xtype: 'processes-bulk-step5',
            buttonsConfig: {
                prevbuttonDisabled: true,
                nextbuttonDisabled: true,
                nextActionPreparationButtonDisabled: true,
                cancelbuttonDisabled: true,
                confirmbuttonDisabled: true,
                finishSuccesfullButtonDisabled: false,

                prevbuttonVisible: false,
                nextbuttonVisible: false,
                nextActionPreparationButtonVisible: false,
                cancelbuttonVisible: false,
                confirmbuttonVisible: false,
                finishSuccesfullButtonVisible: true
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    onCancelButtonClick: function (cancel) {
        var wizard = cancel.up('wizard');
        console.log("onCancelButtonClick is callsed in ProcessesBulkWizard!!!");
        Ext.state.Manager.clear('formAssignValues');
        Ext.state.Manager.clear('formCloseValues');
        wizard.fireEvent('wizardcancelled', wizard);
    },

    onConfirmButtonClick: function (finish) {
        console.log("onConfirmButtonClick in PROCESSBULKWIZARD!!!!!!!!!!!!!!!!!!!!");
        var wizard = finish.up('wizard');
        var docked = wizard.getDockedItems('toolbar[dock="bottom"]')[0];
        //docked.setVisible(false);

        wizard.getLayout().setActiveItem(++wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
        wizard.fireEvent('wizardfinished', wizard);
    },

    onNextButtonClick: function (next) {
        console.log("NEXT =",next);
        var wizard = next.up('wizard'),
            functionName = 'processValidateOnStep' + (wizard.activeItemId + 1);
            console.log('wizard=',wizard);
        if (this.processValidate(functionName, wizard)) {
            console.log('setActiveItem!!! wizard.getLayout()=',wizard.getLayout());
            wizard.getLayout().setActiveItem(++wizard.activeItemId);
            console.log('fireEvent!!!');
            wizard.fireEvent('wizardpagechange', wizard);
            wizard.fireEvent('wizardnext', wizard);
        }
    },

    /* XROMVYU  callback!!!!!!!!!!!!*/
    onPrepareActionButtonClick: function (finish) {
        var wizard = finish.up('wizard');
        //var docked = wizard.getDockedItems('toolbar[dock="bottom"]')[0];
        //docked.setVisible(false);

        wizard.fireEvent('wizarpreparationfinished', wizard);
    },

    onFinishButtonClick: function (finish) {
        var wizard = finish.up('wizard');
        //var docked = wizard.getDockedItems('toolbar[dock="bottom"]')[0];
        //docked.setVisible(false);

//        wizard.getLayout().setActiveItem(++wizard.activeItemId);
        wizard.fireEvent('wizaractionfinished', wizard);
    },

    processValidate: function (func, wizard) {
        if (func in this) {
            return this[func](wizard);
        } else {
            return true;
        }
    },

    processValidateOnStep1: function (wizard) {
        var processesGrid = wizard.down('processes-selection-grid'/*'issues-selection-grid'*/),
            step1ErrorPanel = wizard.down('[name=step1-errors]'),
            gridError = wizard.down('#selection-grid-error');

        if (!processesGrid.isAllSelected() && Ext.isEmpty(processesGrid.view.getSelectionModel().getSelection())) {
            gridError.show();
            step1ErrorPanel.setVisible(true);
            return false;
        } else {
            step1ErrorPanel.setVisible(false);
            gridError.hide();
            return true;
        }
    },

    processValidateOnStep3: function (wizard) {
        return true;
    }

});
