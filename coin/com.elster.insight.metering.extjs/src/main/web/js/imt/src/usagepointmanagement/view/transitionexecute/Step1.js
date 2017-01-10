Ext.define('Imt.usagepointmanagement.view.transitionexecute.Step1', {
    extend: 'Ext.form.Panel',
    xtype: 'usagepointtransitionexecute-wizard-step1',
    ui: 'large',
    requires: [
        'Imt.usagepointmanagement.view.transitionexecute.TransitionDateField'
    ],
    layout: {
        type: 'vbox',
        align: 'stretchmax'
    },
    title: Uni.I18n.translate('usagepointtransitionexecute.wizard.step1.title', 'IMT', 'Step 1: Set properties'),

    items: [
        {
            xtype: 'uni-form-error-message',
            itemId: 'form-errors',            
            hidden: true,
            width: 800
        },
        {
            xtype: 'transition-date-field',
            labelWidth: 260,
            itemId: 'transitionDateField',
            fieldLabel: Uni.I18n.translate('usagepointtransitionexecute.wizard.transitiondate', 'IMT', 'Transition date')
        },
        {
            xtype: 'property-form',
            itemId: 'transition-property-form',
            defaults: {
                labelWidth: 260,
                resetButtonHidden: true
            }
        }
    ]
});