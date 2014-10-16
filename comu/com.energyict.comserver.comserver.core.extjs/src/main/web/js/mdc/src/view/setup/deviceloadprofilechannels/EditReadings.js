Ext.define('Mdc.view.setup.deviceloadprofilechannels.EditReadings', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-loadprofile-channel-edit-readings',
    itemId: 'device-loadprofile-channel-edit-readings',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceloadprofilechannels.EditReadingsGrid'
    ],

    router: null,
    channel: null,

    initComponent: function () {
        var me = this;
        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('deviceloadprofilechannels.editReadings.editChannelReadings', 'MDC', 'Edit channel readings'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-loadprofile-channel-edit-readings-grid',
                        itemId: 'device-loadprofile-channel-edit-readings-grid',
                        channel: me.channel,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceloadprofilechannels.editReadings.empty.title', 'MDC', 'No readings found'),
                        reasons: [
                            Uni.I18n.translate('deviceloadprofilechannels.editReading.empty.list.item1', 'MDC', 'No readings have been defined yet.'),
                            Uni.I18n.translate('deviceloadprofilechannels.editReading.empty.list.item2', 'MDC', 'No readings comply to the filter.')
                        ]
                    },
                    previewComponent: null
                }
            ]
        };

        me.callParent(arguments);
    }
});

