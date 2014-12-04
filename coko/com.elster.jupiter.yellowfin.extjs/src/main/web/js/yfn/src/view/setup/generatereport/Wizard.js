Ext.define('Yfn.view.setup.generatereport.Wizard', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.layout.container.Card',
        'Yfn.view.setup.generatereport.Step1',
        'Yfn.view.setup.generatereport.Step2',
        'Yfn.view.setup.generatereport.Step3',
        'Yfn.view.setup.generatereport.Step4'

    ],
    alias: 'widget.generatereport-wizard',
    autoHeight: true,
    border: false,
    layout: 'card',
    activeItemId: 0,
    buttonAlign: 'left',
    items: [
        {
            xtype: 'generatereport-wizard-step1',
            itemId: 'generatereport-wizard-step1',
            navigationIndex: 0
        },
        {
            xtype: 'generatereport-wizard-step2',
            itemId: 'generatereport-wizard-step2',
            navigationIndex: 1
        },
        {
            xtype: 'generatereport-wizard-step3',
            itemId: 'generatereport-wizard-step3',
            navigationIndex: 2
        },
        {
            xtype: 'generatereport-wizard-step4',
            itemId: 'generatereport-wizard-step4',
            navigationIndex: 3
        }
    ],
    bbar: {
        defaults: {
            xtype: 'button'
        },
        items: [
            {
                text: Uni.I18n.translate('general.back', 'MDC', 'Back'),
                action: 'back',
                itemId: 'backButton',
                disabled: true
            },
            {
                text: Uni.I18n.translate('general.next', 'MDC', 'Next'),
                ui: 'action',
                action: 'next',
                itemId: 'nextButton'
            },
            {
                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                ui: 'action',
                action: 'finish',
                itemId: 'finishButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                ui: 'link',
                action: 'cancel',
                itemId: 'wizardCancelButton',
                href: ''
            }
        ]
    }
});
