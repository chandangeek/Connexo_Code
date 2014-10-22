Ext.define('Mdc.view.setup.devicegroup.Step2', {
    extend: 'Ext.panel.Panel',
    xtype: 'devicegroup-wizard-step2',
    name: 'deviceGroupWizardStep2',
    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.view.setup.devicesearch.SearchItems'

    ],


    title: Uni.I18n.translate('devicegroup.wizard.step2title', 'MDC', 'Add a device group - Step 2 of 2: Device group'),

    items: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('setup.devicesearch.searchResults.title', 'MDC', 'Search')
        },
        {
            xtype: 'filter-top-panel'
        },
        {
            xtype: 'mdc-search-results'
        }
    ]


});
