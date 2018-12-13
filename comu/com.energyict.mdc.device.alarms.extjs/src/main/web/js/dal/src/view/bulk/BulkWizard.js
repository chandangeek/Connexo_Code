Ext.define('Dal.view.bulk.BulkWizard', {
    extend: 'Dal.view.bulk.Wizard',
    alias: 'widget.alarm-bulk-wizard',
    itemId: 'alarm-bulk-wizard',
    titlePrefix: Uni.I18n.translate('general.title.bulkActions', 'DAL', 'Bulk action'),
    includeSubTitle: true,

    requires: [
        'Dal.view.bulk.Step1',
        'Dal.view.bulk.Step2',
        'Dal.view.bulk.Step3',
        'Dal.view.bulk.Step4',
        'Dal.view.bulk.Step5'
    ],

    title: 'Wizard',
    ui: 'large',

    items: [
        {
            itemId: 'alarm-bulk-step1',
            xtype: 'alarm-bulk-step1',
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
            itemId: 'alarm-bulk-step2',
            xtype: 'alarm-bulk-step2',
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
            itemId: 'alarm-bulk-step3',
            xtype: 'alarm-bulk-step3',
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
            itemId: 'alarm-bulk-step4',
            xtype: 'alarm-bulk-step4',
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
            itemId: 'alarm-bulk-step5',
            xtype: 'alarm-bulk-step5',
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
        var alarmsGrid = wizard.down('alarms-selection-grid'),
            step1ErrorPanel = wizard.down('[name=step1-errors]'),
            gridError = wizard.down('#selection-grid-error');

        if (!alarmsGrid.isAllSelected() && Ext.isEmpty(alarmsGrid.view.getSelectionModel().getSelection())) {
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