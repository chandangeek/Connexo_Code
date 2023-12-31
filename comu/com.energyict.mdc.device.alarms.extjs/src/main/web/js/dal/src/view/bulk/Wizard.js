Ext.define('Dal.view.bulk.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.wizard',
    autoHeight: true,
    border: false,
    layout: 'card',
    activeItemId: 0,
    inWizard: false,
    includeSubTitle: false,
    buttonAlign: 'left',

    requires: [
        'Ext.layout.container.Card'
    ],

    listeners: {
        render: function () {
            if (this.includeSubTitle) {
                this.setTitle(Uni.I18n.translate('issue.title.steps', 'DAL', 'Step {0}', 1) + ': ' + this.getActiveItem().title);
            } else {
                this.setTitle(Uni.I18n.translate('issue.title.steps', 'DAL', 'Step {0}', 1));
            }
            this.inWizard = true;
            this.setButtonsState(this);
            this.fireEvent('wizardstarted', this);
        },

        beforerender: function () {
            Ext.each(this.getLayout().getLayoutItems(), function (card) {
                card.preventHeader = true;
            });
        },

        wizardpagechange: function (wizard) {
            this.onWizardPageChangeEvent(wizard);
        }
    },

    onWizardPageChangeEvent: function (wizard) {
        if (this.includeSubTitle) {
            wizard.getActiveItem().preventHeader = true;
            wizard.setTitle(Uni.I18n.translate('alarm.title.steps', 'DAL', 'Step {0}', wizard.activeItemId + 1) + ': ' + this.getActiveItem().title);
        } else {
            wizard.setTitle(Uni.I18n.translate('alarm.title.steps', 'DAL', 'Step {0}', wizard.activeItemId + 1));
        }

        this.setButtonsState(wizard);
    },

    setButtonsState: function (wizard) {
        var toolbar = wizard.down('toolbar[name="wizar-toolbar"]');
        var activeItem = wizard.getActiveItem();

        toolbar.child('#prev').setDisabled(activeItem.buttonsConfig.prevbuttonDisabled);
        toolbar.child('#next').setDisabled(activeItem.buttonsConfig.nextbuttonDisabled);
        toolbar.child('#cancel').setDisabled(activeItem.buttonsConfig.cancelbuttonDisabled);
        toolbar.child('#finish').setDisabled(activeItem.buttonsConfig.confirmbuttonDisabled);

        toolbar.child('#prev').setVisible(activeItem.buttonsConfig.prevbuttonVisible);
        toolbar.child('#next').setVisible(activeItem.buttonsConfig.nextbuttonVisible);
        toolbar.child('#cancel').setVisible(activeItem.buttonsConfig.cancelbuttonVisible);
        toolbar.child('#finish').setVisible(activeItem.buttonsConfig.confirmbuttonVisible);
    },

    onPrevButtonClick: function (prev) {
        var wizard = prev.up('wizard');
        if (!wizard.getForm().isValid()) {
            var fields = wizard.down('assign-issue').getForm().getFields();
            fields.each(function (field) {
                field.disable();
            });
        } else if (wizard.down('assign-issue')) {
            /*var assignValues = wizard.getForm().getValues(),
                assignRadio = wizard.down('issues-assign-form').down('radiogroup').items.items[0].getGroupValue();
            Ext.state.Manager.set('formAssignRadio', assignRadio);
            Ext.state.Manager.set('formAssignValues', assignValues);*/
        } else if (wizard.down('issues-close-form')) {
            var closeValues = wizard.getForm().getValues(),
                closeRadio = wizard.down('issues-close-form').down('radiogroup').items.items[0].getGroupValue();
            Ext.state.Manager.set('formCloseRadio', closeRadio);
            Ext.state.Manager.set('formCloseValues', closeValues);
        }else if (wizard.down('set-priority-form')) {
            var setPriorityValues = wizard.getForm().getValues();
            Ext.state.Manager.set('formSetPriorityValues', setPriorityValues);
        }
        wizard.getLayout().setActiveItem(--wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
        wizard.fireEvent('wizardprev', wizard);
    },

    onNextButtonClick: function (next) {
        var wizard = next.up('wizard');
        wizard.getLayout().setActiveItem(++wizard.activeItemId);
        wizard.fireEvent('wizardpagechange', wizard);
        wizard.fireEvent('wizardnext', wizard);
    },

    onConfirmButtonClick: function (finish) {
        var wizard = finish.up('wizard');
        if (!wizard.getForm().isValid()) {
            var invalidFields = Uni.I18n.translate('alarm.correctErrors.beforeResume','DAL','Please correct the following errors before resumitting<br>');
            wizard.getForm().getFields().each(function (field) {
                if (!field.isValid()) {
                    invalidFields += '<br><b>' + field.getFieldLabel() + '</b>';
                    invalidFields += '<br>' + field.getErrors(field.getValue());
                    invalidFields += '<br>';
                }
            });
            Ext.Msg.show({
                scope: this,
                title: Uni.I18n.translate('alarm.wizardInvalid','DAL','Wizard Invalid'),
                msg: invalidFields,
                buttons: Ext.Msg.OK,
                icon: Ext.Msg.ERROR
            });
        } else {
            wizard.inWizard = false;
            wizard.fireEvent('wizardfinished', wizard);
        }
    },

    onCancelButtonClick: function (cancel) {
        var wizard = cancel.up('wizard');
        if (wizard.getForm().isDirty()) {
            Ext.Msg.show({
                scope: this,
                title: Uni.I18n.translate('alarm.cancellingWizard','DAL','Cancelling Wizard'),
                msg: Uni.I18n.translate('alarm.cancellingWizard.msg', 'DAL', 'All changes will be lost. Are you sure you want to cancel?'),
                buttons: Ext.Msg.YESNO,
                icon: Ext.Msg.QUESTION,
                fn: function (buttonId, text, opt) {
                    switch (buttonId) {
                        case 'yes':
                            wizard.fireEvent('wizardcancelled', wizard);
                            break;
                        case 'no':
                            break;
                    }
                }
            });
        } else {
            wizard.fireEvent('wizardcancelled', wizard);
        }
    },

    getActiveItem: function () {
        return this.items.items[this.activeItemId];
    },

    getActiveItemId: function () {
        return this.activeItemId;
    },

    initComponent: function () {
        Ext.apply(this, {
            dockedItems: [
                {
                    itemId: 'toolbarbot',
                    xtype: 'toolbar',
                    name: 'wizar-toolbar',
                    dock: 'bottom',
                    border: false,
                    items: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.back', 'DAL', 'Back'),
                            itemId: 'prev',
                            action: 'prevWizard',
                            scope: this,
                            handler: this.onPrevButtonClick
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.next', 'DAL', 'Next'),
                            itemId: 'next',
                            action: 'nextWizard',
                            scope: this,
                            handler: this.onNextButtonClick,
                            ui: 'action'

                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.confirm', 'DAL', 'Confirm'),
                            itemId: 'finish',
                            action: 'finishWizard',
                            hidden: true,
                            scope: this,
                            handler: this.onConfirmButtonClick,
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.cancel', 'DAL', 'Cancel'),
                            ui: 'link',
                            itemId: 'cancel',
                            action: 'cancelWizard',
                            scope: this,
                            handler: this.onCancelButtonClick
                        }
                    ]
                }
            ]
        });

        this.callParent(arguments);
    }
});