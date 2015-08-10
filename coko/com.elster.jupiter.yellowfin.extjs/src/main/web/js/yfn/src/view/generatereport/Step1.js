Ext.define('Yfn.view.generatereport.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'generatereport-wizard-step1',
    name: 'generateReportWizardStep1',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormInfoMessage',
        'Ext.form.RadioGroup'

    ],

    title: Uni.I18n.translate('generatereport.wizard.step1title', 'YFN', 'Step 1 of 3: Select report type'),

    items: [
        {
            itemId: 'step1-generatereport-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('generatereport.noReportPrompts', 'YFN', 'Please select at least one report.')
        },
        {
            xtype: 'form',
            layout: 'column',
            itemId: 'step1-form'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});