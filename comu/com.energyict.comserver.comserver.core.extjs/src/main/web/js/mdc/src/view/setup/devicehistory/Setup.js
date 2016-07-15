Ext.define('Mdc.view.setup.devicehistory.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-history-setup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.devicehistory.LifeCycle'
    ],

    router: null,
    device: null,
    activeTab: null,

    initComponent: function () {
        var me = this;

        me.side = {
            ui: 'medium',
            items: [
                {
                    xtype: 'deviceMenu',
                    itemId: 'device-history-side-menu',
                    device: me.device
                }
            ]
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.history', 'MDC', 'History'),
            itemId: 'history-panel',
            items: [
                {
                    xtype: 'tabpanel',
                    margin: '20 0 0 0',
                    itemId: 'device-history-tab-panel',
                    activeTab: me.activeTab,
                    width: '100%',
                    items: [
                        {
                            title: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                            padding: '8 16 16 0',
                            itemId: 'device-history-life-cycle-tab'
                        },
                        {
                            title: Uni.I18n.translate('general.firmware', 'MDC', 'Firmware'),
                            padding: '8 16 16 0',
                            itemId: 'device-history-firmware-tab'
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    },

    loadCustomAttributeSets: function (customAttributeSetsStore) {
        var me = this;

        Ext.suspendLayouts();
        customAttributeSetsStore.each(function (customAttributeSet) {
            if (customAttributeSet.get('timesliced')) {
                me.down('#device-history-tab-panel').add(
                    {
                        title: customAttributeSet.get('name'),
                        itemId: 'custom-attribute-set-' +  customAttributeSet.get('id'),
                        customAttributeSetId: customAttributeSet.get('id')
                    }
                )
            }
        });
        Ext.resumeLayouts(true);
    }
});
