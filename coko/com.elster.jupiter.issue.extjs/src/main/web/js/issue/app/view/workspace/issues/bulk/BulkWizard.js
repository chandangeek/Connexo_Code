Ext.define('Mtr.view.workspace.issues.bulk.BulkWizard', {
    extend: 'Mtr.view.workspace.issues.bulk.Wizard',
    alias: 'widget.bulk-wizard',
    titlePrefix: 'Bulk action',
    includeSubTitle: true,

    requires: [
        'Mtr.view.workspace.issues.bulk.Step1',
        'Mtr.view.workspace.issues.bulk.Step2',
        'Mtr.view.workspace.issues.bulk.Step3',
        'Mtr.view.workspace.issues.bulk.Step4',
        'Mtr.view.workspace.issues.bulk.Step5'
    ],

    defaults: {
        border: false,
        overflowY: 'auto'
    },

    items: [
        {
            xtype: 'bulk-step1',
            buttonsConfig : {
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
            buttonsConfig : {
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
            buttonsConfig : {
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
            buttonsConfig : {
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
            buttonsConfig : {
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

    onConfirmButtonClick: function(finish) {
        var wizard = finish.up('wizard');
        var docked = wizard.getDockedItems('toolbar[dock="bottom"]')[0];
        docked.setVisible(false);

        wizard.getLayout().setActiveItem(++wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
        wizard.fireEvent('wizardfinished', wizard);
    }
});