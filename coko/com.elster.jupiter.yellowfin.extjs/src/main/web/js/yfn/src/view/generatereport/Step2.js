Ext.define('Yfn.view.generatereport.Step2', {
    extend: 'Ext.panel.Panel',
    xtype: 'generatereport-wizard-step2',
    name: 'generateReportWizardStep2',
    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    requires: [
        'Uni.util.FormErrorMessage'
    ],


    title: Uni.I18n.translate('generatereport.wizard.step2title', 'YFN', 'Step 2 of 4:  Select report prompts'),

    items: [
        {
            itemId: 'step2-generatereport-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('generatereport.noReportPrompts', 'MDC', 'Please select at least one device.')
        },
        {
            xtype: 'filter-top-panel'
        },
        {
            xtype: 'form',
            layout: 'column',
            itemId: 'step2-form'
        }
    ]


});
