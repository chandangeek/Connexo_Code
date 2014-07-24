Ext.define('Mdc.view.setup.deviceregisterdata.MainSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceMainRegisterReportSetup',
    itemId: 'deviceMainRegisterReportSetup',

    mRID: null,
    registerId: null,

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