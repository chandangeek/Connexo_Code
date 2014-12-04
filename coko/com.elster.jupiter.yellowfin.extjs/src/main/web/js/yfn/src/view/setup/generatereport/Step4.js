Ext.define('Yfn.view.setup.generatereport.Step4', {
    extend: 'Ext.panel.Panel',
    xtype: 'generatereport-wizard-step4',
    name: 'deviceGroupWizardStep4',
    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    requires: [
        'Uni.util.FormErrorMessage'
    ],


    title: Uni.I18n.translate('generatereport.wizard.step4title', 'MDC', 'Step 4 of 4:  Generate selected report'),

    items: [
        {
            itemId: 'step4-generatereport-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('generatereport.noDevicesSelected', 'MDC', 'Please select at least one device.')
        },
        {
            xtype: 'filter-top-panel'
        }
    ]


});
