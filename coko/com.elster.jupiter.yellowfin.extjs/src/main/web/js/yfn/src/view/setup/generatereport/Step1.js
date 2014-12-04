Ext.define('Mdc.view.setup.generatereport.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'generatereport-wizard-step1',
    name: 'generateReportWizardStep1',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Ext.form.RadioGroup'

    ],

    title: Uni.I18n.translate('generatereport.wizard.step1title', 'MDC', 'Step 1 of 4: Select report type'),

    items: [
        {
            itemId: 'step2-generatereport-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('generatereport.noReportPrompts', 'MDC', 'Please select at least one device.')
        },
        {
            xtype: 'container',
            layout: 'column',
            itemId: 'step1-generatereport-report-groups'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});