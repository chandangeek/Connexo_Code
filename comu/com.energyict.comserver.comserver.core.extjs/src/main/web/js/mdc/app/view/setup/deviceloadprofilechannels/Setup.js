Ext.define('Mdc.view.setup.deviceloadprofilechannels.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelsSetup',
    itemId: 'deviceLoadProfileChannelsSetup',

    mRID: null,
    loadProfileId: null,
    router: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceloadprofiles.SubMenuPanel',
        'Mdc.view.setup.deviceloadprofilechannels.Grid',
        'Mdc.view.setup.deviceloadprofilechannels.Preview'
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'deviceLoadProfilesSubMenuPanel',
                router: me.router
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceLoadProfileChannelsGrid',
                        mRID: me.mRID,
                        loadProfileId: me.loadProfileId,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceloadprofilechannels.empty.title', 'MDC', 'No channels found'),
                        reasons: [
                            Uni.I18n.translate('deviceloadprofilechannels.empty.list.item1', 'MDC', 'No channels have been defined yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'deviceLoadProfileChannelsPreview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});