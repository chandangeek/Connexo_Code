Ext.define('Isu.view.workspace.issues.bulk.BulkWizard', {
    extend: 'Isu.view.workspace.issues.bulk.Wizard',
    alias: 'widget.bulk-wizard',
    titlePrefix: 'Bulk action',
    includeSubTitle: true,

    requires: [
        'Isu.view.workspace.issues.bulk.Step1',
        'Isu.view.workspace.issues.bulk.Step2',
        'Isu.view.workspace.issues.bulk.Step3',
        'Isu.view.workspace.issues.bulk.Step4',
        'Isu.view.workspace.issues.bulk.Step5'
    ],

    header: {
        title: 'Wizard',
        cls: 'isu-wizard-header',
        style: {
            padding: '15px'
        }
    },

    items: [
        {
            xtype: 'bulk-step1',
            cls: 'bulk-step',
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
            xtype: 'bulk-step2',
            cls: 'bulk-step',
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
            xtype: 'bulk-step3',
            cls: 'bulk-step',
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
            xtype: 'bulk-step4',
            cls: 'bulk-step',
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
            xtype: 'bulk-step5',
            cls: 'bulk-step',
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
        var issuesGrid = wizard.down('issues-list'),
            step1ErrorPanel = wizard.down('[name=step1-errors]'),
            step1RadioGroup = wizard.down('radiogroup');

        if (Ext.isEmpty(issuesGrid.view.getSelectionModel().getSelection())) {
            step1RadioGroup.query('[inputValue=SELECTED]')[0].setBoxLabel('<b>Selected issues<br/><span style="color: #CF4C35;">' +
                'It is required to select one or more issues to go to the next step</b></span>');
            step1ErrorPanel.setVisible(true);
            return false;
        } else {
            return true;
        }
    },

    processValidateOnStep3: function (wizard) {
        var assignForm = wizard.down('bulk-step3').down('issues-assign-form'),
            formErrorsPanel,
            activeRadioButton,
            comboBox;

        if (!Ext.isEmpty(assignForm)) {
            formErrorsPanel = assignForm.down('[name=form-errors]');
            formErrorsPanel.hide();
            formErrorsPanel.removeAll();
            activeRadioButton = assignForm.down('radiogroup').down('[checked=true]')
            comboBox = wizard.down('bulk-step3').down('issues-assign-form').down('combobox[name=' + activeRadioButton.inputValue + ']');
            if (Ext.isEmpty(comboBox.getValue())) {
                formErrorsPanel.add({
                    html: 'You must choose \'' + activeRadioButton.boxLabel + '\' before you can proceed'
                });
                formErrorsPanel.show();
                return false;
            }
        }
        return true;
    }

});