Ext.define('Mdc.view.setup.deviceregisterconfiguration.GeneralDetail', {
    extend: 'Uni.view.container.ContentContainer',

    mRID: null,
    registerId: null,

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.Menu'
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
                        toggle: 0
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});