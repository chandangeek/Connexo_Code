Ext.define('Mdc.view.setup.devicegroup.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.adddevicegroup-wizard',

    requires: [
        'Ext.layout.container.Card',
        'Mdc.view.setup.devicegroup.Step1',
        'Mdc.view.setup.devicegroup.Step2'
    ],

    autoHeight: true,
    border: false,
    layout: 'card',
    activeItemId: 0,
    buttonAlign: 'left',

    defaults: {
        cls: 'content-wrapper'
    },

    items: [
        {
            xtype: 'devicegroup-wizard-step1',
            itemId: 'devicegroup-wizard-step1',
            navigationIndex: 0
        },
        {
            xtype: 'devicegroup-wizard-step2',
            itemId: 'devicegroup-wizard-step2',
            navigationIndex: 1
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
