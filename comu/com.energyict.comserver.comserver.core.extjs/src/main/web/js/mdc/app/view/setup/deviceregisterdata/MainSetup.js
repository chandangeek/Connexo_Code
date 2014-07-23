Ext.define('Mdc.view.setup.deviceregisterdata.MainSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceMainRegisterReportSetup',
    itemId: 'deviceMainRegisterReportSetup',

    mRID: null,
    registerId: null,

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('device.registerData.title', 'MDC', 'Register data'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'devicevalueregisterreportgrid'
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
                        xtype: 'devicevalueregisterreportpreview'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('deviceregisterconfiguration.registers', 'MDC', 'Registers'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceRegisterConfigurationMenu',
                        itemId: 'stepsMenu',
                        mRID: me.mRID,
                        registerId: me.registerId,
                        toggle: 1
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});