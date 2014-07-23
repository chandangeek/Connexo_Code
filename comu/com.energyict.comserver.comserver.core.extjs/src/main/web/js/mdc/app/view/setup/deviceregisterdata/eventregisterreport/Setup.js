Ext.define('Mdc.view.setup.deviceregisterdata.eventregisterreport.Setup', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainSetup',
    alias: 'widget.event-deviceregisterreportsetup',
    itemId: 'deviceEventRegisterReportSetup',

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('device.registerData.title', 'MDC', 'Register data'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceeventregisterreportgrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('device.registerData.title', 'MDC', 'No readings found'),
                        reasons: [
                            Uni.I18n.translate('device.registerData.list.item1', 'MDC', 'No readings have been defined yet.'),
                            Uni.I18n.translate('device.registerData.list.item2', 'MDC', 'No readings comply to the filter.')
                        ],
                        stepItems: []
                    },
                    previewComponent: {
                        xtype: 'deviceeventregisterreportpreview'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});