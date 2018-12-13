/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.bulk.BulkWizard', {
    extend: 'Isu.view.issues.bulk.Wizard',
    alias: 'widget.bulk-wizard',
    itemId: 'bulk-wizard',
    titlePrefix: Uni.I18n.translate('general.title.bulkActions', 'ISU', 'Bulk action'),
    includeSubTitle: true,

    requires: [
        'Isu.view.issues.bulk.Step1',
        'Isu.view.issues.bulk.Step2',
        'Isu.view.issues.bulk.Step3',
        'Isu.view.issues.bulk.Step4',
        'Isu.view.issues.bulk.Step5'
    ],

    title: 'Wizard',
    ui: 'large',

    items: [
        {
            itemId: 'bulk-step1',
            xtype: 'bulk-step1',
            buttonsConfig: {
                prevbuttonDisabled: true,
                nextbuttonDisabled: false,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: true,
                cancelbuttonVisible: true,
                confirmbuttonVisible: false
            }
        },
        {
            itemId: 'bulk-step2',
            xtype: 'bulk-step2',
            buttonsConfig: {
                prevbuttonDisabled: false,
                nextbuttonDisabled: false,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: true,
                cancelbuttonVisible: true,
                confirmbuttonVisible: false
            }
        },
        {
            itemId: 'bulk-step3',
            xtype: 'bulk-step3',
            buttonsConfig: {
                prevbuttonDisabled: false,
                nextbuttonDisabled: false,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: true,

                prevbuttonVisible: true,
                nextbuttonVisible: true,
                cancelbuttonVisible: true,
                confirmbuttonVisible: false
            }
        },
        {
            itemId: 'bulk-step4',
            xtype: 'bulk-step4',
            buttonsConfig: {
                prevbuttonDisabled: false,
                nextbuttonDisabled: true,
                cancelbuttonDisabled: false,
                confirmbuttonDisabled: false,

                prevbuttonVisible: true,
                nextbuttonVisible: false,
                cancelbuttonVisible: true,
                confirmbuttonVisible: true
            }
        },
        {
            itemId: 'bulk-step5',
            xtype: 'bulk-step5',
            buttonsConfig: {
                prevbuttonDisabled: true,
                nextbuttonDisabled: true,
                cancelbuttonDisabled: true,
                confirmbuttonDisabled: true,

                prevbuttonVisible: false,
                nextbuttonVisible: false,
                cancelbuttonVisible: false,
                confirmbuttonVisible: false
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
        docked.setVisible(false);

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

    processValidate: function (func, wizard) {
        if (func in this) {
            return this[func](wizard);
        } else {
            return true;
        }
    },

    processValidateOnStep1: function (wizard) {
        var issuesGrid = wizard.down('issues-selection-grid'),
            step1ErrorPanel = wizard.down('[name=step1-errors]'),
            gridError = wizard.down('#selection-grid-error');

        if (!issuesGrid.isAllSelected() && Ext.isEmpty(issuesGrid.view.getSelectionModel().getSelection())) {
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