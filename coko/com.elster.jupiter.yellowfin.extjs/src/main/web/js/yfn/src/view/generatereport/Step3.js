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


    title: Uni.I18n.translate('generatereport.wizard.step3title', 'YFN', 'Step 3 of 3:  Generate selected report'),

    items: [
        {
            xtype: 'container',
            itemId: 'wizard-summary'
        },
        {
            xtype: 'component',
            itemId: 'wizard-generatereport-link',
            visible:false,
            autoEl: {
                tag: 'a',
                target: '_blank',
                href: 'about:blank',
                html: ''
            }
        }
    ]


});
