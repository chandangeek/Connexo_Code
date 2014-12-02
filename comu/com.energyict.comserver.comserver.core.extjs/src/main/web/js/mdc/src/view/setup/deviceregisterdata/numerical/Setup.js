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
                items: [
                    {
                        xtype: 'filter-top-panel',
                        itemId: 'deviceregisterdatafilterpanel',
                        emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
                    },
                    {
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
                                    stepItems: [
                                        {
                                            text:  Uni.I18n.translate('device.registerData.addReading','MDC','Add reading'),
                                            href: '#/devices/' + me.mRID + '/registers/' + me.registerId + '/data/create'
                                        }
                                    ]
                                },
                                previewComponent: {
                                    xtype: 'deviceregisterreportpreview-numerical'
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});