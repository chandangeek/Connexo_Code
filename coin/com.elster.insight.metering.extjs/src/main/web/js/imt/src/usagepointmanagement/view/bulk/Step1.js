Ext.define('Imt.usagepointmanagement.view.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    xtype: 'usagepoints-bulk-step1',
    name: 'selectDevices',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Imt.usagepointmanagement.view.bulk.UsagePointsSelectionGrid'
    ],

    title: Uni.I18n.translate('usagePoints.bulk.step1.title', 'IMT', 'Step 1: Select usage points'),

    initComponent: function () {
        this.items = [
            {
                xtype: 'panel',
                layout: {
                    type: 'vbox',
                    align: 'left'
                },
                width: '100%',
                items: [
                    {
                        itemId: 'step1-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true,
                        text: Uni.I18n.translate('searchItems.bulk.devicesError', 'IMT', 'It is required to select one or more usage points to go to the next step.')
                    }
                ]
            },
            {
                xtype: 'usagepoints-selection-grid',
                store: this.deviceStore,
                itemId: 'devicesgrid'
            },
            {
                xtype: 'container',
                itemId: 'stepSelectionError',
                margin: '-20 0 0 0',
                hidden: true,
                html: '<span style="color: #eb5642">' + Uni.I18n.translate('searchItems.bulk.selectatleast1usagepoint', 'IMT', 'Select at least 1 usage point') + '</span>'
            }
        ];

        this.callParent(arguments);
    }

});