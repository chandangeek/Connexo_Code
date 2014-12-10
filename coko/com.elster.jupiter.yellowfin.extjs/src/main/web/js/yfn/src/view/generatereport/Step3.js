Ext.define('Yfn.view.generatereport.Step3', {
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
            itemId: 'step3-generatereport-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('generatereport.noDevicesSelected', 'MDC', 'Please select at least one device.')
        },
        {
            xtype: 'uni-form-info-message',
            itemId: 'info-no-fields',
            title: Uni.I18n.translate('generatereport.noReportFilters', 'YFN', 'No report filters defined.'),
            text:Uni.I18n.translate('generatereport.noReportFilter', 'YFN', 'There are no filter defined for this report. You could continue with next step'),
            hidden: true

        },
        {
            xtype: 'form',
            layout: 'column',
            itemId: 'step3-form'
        }
    ]


});
