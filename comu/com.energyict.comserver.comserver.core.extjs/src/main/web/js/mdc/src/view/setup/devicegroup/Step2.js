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
        'Mdc.view.setup.devicesearch.SearchItems',
        'Mdc.view.setup.devicesearch.DevicesTopFilter',
        'Mdc.view.setup.devicesearch.BufferedDevicesTopFilter',
        'Mdc.store.filter.DeviceTypes',
        'Mdc.store.DeviceConfigurations'
    ],


    title: Uni.I18n.translate('devicegroup.wizard.step2title', 'MDC', 'Step 2 of 2: Add device group'),

    items: [
        {
            itemId: 'step2-adddevicegroup-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('devicegroup.noDevicesSelected', 'MDC', 'Please select at least one device.')
        },
        {
            xtype: 'mdc-search-results'
        }
    ],

    dockedItems: [
        {
            xtype: 'mdc-view-setup-devicesearch-devicestopfilter',
            dock: 'top',
            hidden: true
        },
        {
            xtype: 'mdc-view-setup-devicesearch-buffereddevicestopfilter',
            dock: 'top',
            hidden: true
        }
    ]

});
