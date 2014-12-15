Ext.define('Mdc.view.setup.deviceloadprofiles.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfilesSetup',
    itemId: 'deviceLoadProfilesSetup',

    router: null,
    device: null,
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.deviceloadprofiles.Grid',
        'Mdc.view.setup.deviceloadprofiles.Preview'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'loadProfilesLink'
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceloadprofiles.loadProfiles', 'MDC', 'Load profiles'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceLoadProfilesGrid',
                        mRID: me.device.get('mRID'),
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceloadprofiles.empty.title', 'MDC', 'No load profiles found'),
                        reasons: [
                            Uni.I18n.translate('deviceloadprofiles.empty.list.item1', 'MDC', 'No load profiles have been defined yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'deviceLoadProfilesPreview',
                        mRID: me.device.get('mRID'),
                        router: me.router
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});