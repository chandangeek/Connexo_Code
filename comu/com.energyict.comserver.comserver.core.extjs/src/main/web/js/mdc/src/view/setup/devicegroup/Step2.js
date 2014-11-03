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


    title: Uni.I18n.translate('devicegroup.wizard.step2title', 'MDC', 'Step 2 of 2:  Add device group'),

    items: [
        {
            itemId: 'step2-adddevicegroup-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('devicegroup.noDevicesSelected', 'MDC', 'Please select at least one device.')
        },
        {
            xtype: 'filter-top-panel'
        },
        {
            xtype: 'mdc-search-results'
        }
    ]


});
