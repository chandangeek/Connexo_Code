/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.ProcessesBulkWizard', {
    extend: 'Mdc.processes.view.bulk.Wizard',
    alias: 'widget.process-bulk-wizard',
    itemId: 'process-bulk-wizard',
    titlePrefix: Uni.I18n.translate('mdc.processgrid.bulk.title.bulkActions', 'MDC', 'Bulk action'),
    includeSubTitle: true,

    requires: [
        'Mdc.processes.view.bulk.Step1',
        'Mdc.processes.view.bulk.Step2',
        'Mdc.processes.view.bulk.Step3',
        'Mdc.processes.view.bulk.Step4',
        'Mdc.processes.view.bulk.Step5'
    ],

    title: 'Wizard',
    ui: 'large',

    items: [
        {
            itemId: 'processes-bulk-step1',
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
        Ext.state.Manager.clear('formAssignValues');
        Ext.state.Manager.clear('formCloseValues');
        wizard.fireEvent('wizardcancelled', wizard);
    },

    onConfirmButtonClick: function (finish) {
        var wizard = finish.up('wizard');
        var docked = wizard.getDockedItems('toolbar[dock="bottom"]')[0];

        wizard.getLayout().setActiveItem(++wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
        wizard.fireEvent('wizardfinished', wizard);
    },

    onNextButtonClick: function (next) {
        var wizard = next.up('wizard'),
            functionName = 'processValidateOnStep' + (wizard.activeItemId + 1);
        if (this.processValidate(functionName, wizard)) {
            wizard.getLayout().setActiveItem(++wizard.activeItemId);
            wizard.fireEvent('wizardpagechange', wizard);
            wizard.fireEvent('wizardnext', wizard);
        }
    },

    onPrepareActionButtonClick: function (finish) {
        var wizard = finish.up('wizard');
        wizard.fireEvent('wizarpreparationfinished', wizard);
    },

    onFinishButtonClick: function (finish) {
        var wizard = finish.up('wizard');
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
        var processesGrid = wizard.down('processes-selection-grid'),
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