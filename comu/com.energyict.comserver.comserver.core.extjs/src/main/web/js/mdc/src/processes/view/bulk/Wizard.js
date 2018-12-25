/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.bulk.Wizard', {
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
                this.setTitle(Uni.I18n.translate('mdc.process.steps', 'MDC', 'Step {0}', 1) + ': ' + this.getActiveItem().title);
            } else {
                this.setTitle(Uni.I18n.translate('mdc.process.steps', 'MDC', 'Step {0}', 1));
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
            wizard.setTitle(Uni.I18n.translate('mdc.process.steps', 'MDC', 'Step {0}', wizard.activeItemId + 1) + ': ' + this.getActiveItem().title);
        } else {
            wizard.setTitle(Uni.I18n.translate('mdc.process.steps', 'MDC', 'Step {0}', wizard.activeItemId + 1));
        }

        this.setButtonsState(wizard);
    },

    setButtonsState: function (wizard) {
        var toolbar = wizard.down('toolbar[name="wizar-toolbar"]');
        var activeItem = wizard.getActiveItem();

        toolbar.child('#prev').setDisabled(activeItem.buttonsConfig.prevbuttonDisabled);
        toolbar.child('#next').setDisabled(activeItem.buttonsConfig.nextbuttonDisabled);
        toolbar.child('#nextForActionPreparation').setDisabled(activeItem.buttonsConfig.nextActionPreparationButtonDisabled);
        toolbar.child('#cancel').setDisabled(activeItem.buttonsConfig.cancelbuttonDisabled);
        toolbar.child('#finish').setDisabled(activeItem.buttonsConfig.confirmbuttonDisabled);
        toolbar.child('#finishButton').setDisabled(activeItem.buttonsConfig.finishSuccesfullButtonDisabled);



        toolbar.child('#prev').setVisible(activeItem.buttonsConfig.prevbuttonVisible);
        toolbar.child('#next').setVisible(activeItem.buttonsConfig.nextbuttonVisible);
        toolbar.child('#nextForActionPreparation').setVisible(activeItem.buttonsConfig.nextActionPreparationButtonVisible);
        toolbar.child('#cancel').setVisible(activeItem.buttonsConfig.cancelbuttonVisible);
        toolbar.child('#finish').setVisible(activeItem.buttonsConfig.confirmbuttonVisible);
        toolbar.child('#finishButton').setVisible(activeItem.buttonsConfig.finishSuccesfullButtonVisible);
    },

    onPrevButtonClick: function (prev) {
        var wizard = prev.up('wizard');
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

    /* Overwritten in ProcessBulkWizard */
    onConfirmButtonClick: function (finish) {

    },

    /* Overwritten in ProcessBulkWizard */
    onCancelButtonClick: function (cancel) {

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
                            text: Uni.I18n.translate('mdc.process.bulk.back', 'MDC', 'Back'),
                            itemId: 'prev',
                            action: 'prevWizard',
                            scope: this,
                            handler: this.onPrevButtonClick
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('mdc.process.bulk.next', 'MDC', 'Next'),
                            itemId: 'next',
                            action: 'nextWizard',
                            scope: this,
                            handler: this.onNextButtonClick,
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('mdc.process.bulk.next', 'MDC', 'Next'),
                            itemId: 'nextForActionPreparation',
                            scope: this,
                            handler: this.onPrepareActionButtonClick,
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('mdc.process.bulk.confirm', 'MDC', 'Confirm'),
                            itemId: 'finish',
                            action: 'finishWizard',
                            hidden: true,
                            scope: this,
                            handler: this.onConfirmButtonClick,
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('mdc.process.bulk.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            itemId: 'cancel',
                            action: 'cancelWizard',
                            scope: this,
                            handler: this.onCancelButtonClick
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('mdc.process.bulk.finish', 'MDC', 'Finish'),
                            ui: 'action',
                            action: 'finish',
                            itemId: 'finishButton',
                            hidden: true,
                            scope: this,
                            handler: this.onFinishButtonClick
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('mdc.process.bulk.finish', 'MDC', 'Finish'),
                            ui: 'remove',
                            action: 'finish',
                            itemId: 'failureFinishButton',
                            hidden: true
                        },
                    ]
                }
            ]
        });

        this.callParent(arguments);
    }
});