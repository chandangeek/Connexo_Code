Ext.define('Mdc.view.setup.deviceregisterdata.numerical.Setup', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainSetup',
    alias: 'widget.deviceregisterreportsetup-numerical',
    itemId: 'deviceregisterreportsetup',

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('device.registerData.title', 'MDC', 'Register data'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceregisterreportgrid-numerical',
                            mRID: me.mRID,
                            registerId: me.registerId
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
                            xtype: 'deviceregisterreportpreview-numerical'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});