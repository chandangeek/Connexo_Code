Ext.define('Yfn.view.generatereport.Wizard', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.layout.container.Card',
        'Yfn.view.generatereport.Step1',
        'Yfn.view.generatereport.Step2',
        'Yfn.view.generatereport.Step3'
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
        }/*,
        {
            xtype: 'generatereport-wizard-step4',
            itemId: 'generatereport-wizard-step4',
            navigationIndex: 3
        }*/
    ],
    bbar: {
        defaults: {
            xtype: 'button'
        },
        items: [
            {
                text: Uni.I18n.translate('general.back', 'YFN', 'Back'),
                action: 'back',
                itemId: 'backButton',
                disabled: true
            },
            {
                text: Uni.I18n.translate('general.next', 'YFN', 'Next'),
                ui: 'action',
                action: 'next',
                itemId: 'nextButton'
            },
            {
                text: Uni.I18n.translate('general.generate', 'YFN', 'Generate'),
                ui: 'action',
                action: 'finish',
                itemId: 'finishButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.cancel', 'YFN', 'Cancel'),
                ui: 'link',
                action: 'cancel',
                itemId: 'wizardCancelButton',
                href: ''
            }
        ]
    }
});
