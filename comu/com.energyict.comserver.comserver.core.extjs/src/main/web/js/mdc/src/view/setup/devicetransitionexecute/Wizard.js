Ext.define('Mdc.view.setup.devicetransitionexecute.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceTransitionExecuteWizard',

    requires: [
        'Ext.layout.container.Card',
        'Mdc.view.setup.devicetransitionexecute.Step1',
        'Mdc.view.setup.devicetransitionexecute.Step2'
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
            xtype: 'devicetransitionexecute-wizard-step1',
            itemId: 'devicetransitionexecute-wizard-step1',
            navigationIndex: 0
        },
        {
            xtype: 'devicetransitionexecute-wizard-step2',
            itemId: 'devicetransitionexecute-wizard-step2',
            navigationIndex: 1
        }
    ],

    bbar: {
        defaults: {
            xtype: 'button'
        },
        items: [
            {
                text: Uni.I18n.translate('general.next', 'MDC', 'Next'),
                ui: 'action',
                action: 'next',
                itemId: 'nextButton'
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
