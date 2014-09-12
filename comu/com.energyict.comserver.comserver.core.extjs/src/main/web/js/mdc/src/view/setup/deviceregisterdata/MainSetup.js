Ext.define('Mdc.view.setup.deviceregisterdata.MainSetup', {
    extend: 'Uni.view.container.ContentContainer',

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

        me.callParent(arguments);
    }
});