Ext.define('Mdc.view.setup.deviceregisterdata.MainSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceRegisterDataPage',
    mRID: null,
    registerId: null,
    requires: [
        'Mdc.view.setup.deviceregisterdata.SideFilter'
    ],
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                itemId: 'sideRegisterPanel',
                ui: 'medium',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        ui: 'medium',
                        title: Uni.I18n.translate('deviceregisterconfiguration.registers', 'MDC', 'Registers'),
                        itemId: 'sidePanelForSubMenu',
                        items: [
                            {
                                xtype: 'deviceRegisterConfigurationMenu',
                                itemId: 'stepsMenu',
                                mRID: me.mRID,
                                registerId: me.registerId,
                                toggle: 1
                            }
                        ]
                    },
                    {
                        xtype: 'deviceRegisterDataSideFilter',
                        hidden: true
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});