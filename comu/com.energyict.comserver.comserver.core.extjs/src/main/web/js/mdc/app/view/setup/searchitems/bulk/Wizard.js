Ext.define('Mdc.view.setup.searchitems.bulk.Wizard', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.layout.container.Card',
        'Mdc.view.setup.searchitems.bulk.Step1',
        'Mdc.view.setup.searchitems.bulk.Step2',
        'Mdc.view.setup.searchitems.bulk.Step3',
        'Mdc.view.setup.searchitems.bulk.Step4',
        'Mdc.view.setup.searchitems.bulk.Step5'
    ],
    alias: 'widget.searchitems-wizard',
    autoHeight: true,
    border: false,
    layout: 'card',
    activeItemId: 0,
    buttonAlign: 'left',
    items: [
        {
            xtype: 'searchitems-bulk-step1',
            itemId: 'searchitems-bulk-step1'
        },
        {
            xtype: 'searchitems-bulk-step2',
            itemId: 'searchitems-bulk-step2'
        },
        {
            xtype: 'searchitems-bulk-step3',
            itemId: 'searchitems-bulk-step3'
        },
        {
            xtype: 'searchitems-bulk-step4',
            itemId: 'searchitems-bulk-step4'
        },
        {
            xtype: 'searchitems-bulk-step5',
            itemId: 'searchitems-bulk-step5'
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
                text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
                ui: 'action',
                action: 'confirm',
                itemId: 'confirmButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                ui: 'action',
                action: 'finish',
                itemId: 'finishButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                ui: 'link',
                action: 'cancel',
                itemId: 'cancelButton',
                href: ''
            }
        ]
    }
});