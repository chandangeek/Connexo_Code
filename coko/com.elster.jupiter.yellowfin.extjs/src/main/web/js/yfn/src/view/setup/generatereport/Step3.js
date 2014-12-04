Ext.define('Yfn.view.setup.generatereport.Step3', {
    extend: 'Ext.panel.Panel',
    xtype: 'generatereport-wizard-step3',
    name: 'generateReportWizardStep3',
    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    requires: [
        'Uni.util.FormErrorMessage'
    ],


    title: Uni.I18n.translate('generatereport.wizard.step3title', 'MDC', 'Step 3 of 4:  Select report filter'),

    items: [
        {
            itemId: 'step2-generatereport-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('generatereport.noDevicesSelected', 'MDC', 'Please select at least one device.')
        }
    ]


});
