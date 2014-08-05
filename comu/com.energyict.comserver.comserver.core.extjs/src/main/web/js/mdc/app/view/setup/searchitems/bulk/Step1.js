Ext.define('Mdc.view.setup.searchitems.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'searchitems-bulk-step1',
    name: 'selectDevices',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Mdc.view.setup.searchitems.bulk.DevicesSelectionGrid'
    ],

    title: Uni.I18n.translate('searchItems.bulk.step1title', 'MDC', 'Bulk action - step 1 of 5: Select devices'),

    items: [
        {
            itemId: 'step1-errors',
            xtype: 'uni-form-error-message',
            hidden: true,
            text: Uni.I18n.translate('searchItems.bulk.devicesError', 'MDC', 'It is required to select one or more devices to go to the next step')
        },
        {
            xtype: 'devices-selection-grid'
        }
    ]
});