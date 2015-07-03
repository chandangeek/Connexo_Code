Ext.define('Mdc.view.setup.devicetransitionexecute.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'devicetransitionexecute-wizard-step1',
    name: 'devicetransitionWizardStep1',
    ui: 'large',

    requires: [
        'Mdc.view.setup.devicetransitionexecute.form.TransitionDateField'
    ],

    title: Uni.I18n.translate('devicetransitionexecute.wizard.step1title', 'MDC', 'Step 1 of 2: Set properties'),


    items: [
        {
            xtype: 'form',
            itemId: 'transition-form',
            border: false,
            margin: '20 0 0 0',

            items: [
                {
                    xtype: 'transition-date-field',
                    itemId: 'transitionDateField',
                    fieldLabel: Uni.I18n.translate('devicetransitionexecute.wizard.transitiondate', 'MDC', 'Transition date'),
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'property-form',
                    itemId: 'transition-property-form',

                    defaults: {
                        labelWidth: 150,
                        resetButtonHidden: true
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});