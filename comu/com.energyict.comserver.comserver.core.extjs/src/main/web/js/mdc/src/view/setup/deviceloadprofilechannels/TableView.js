Ext.define('Mdc.view.setup.deviceloadprofilechannels.TableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.deviceLoadProfileChannelTableView',
    itemId: 'deviceLoadProfileChannelTableView',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceloadprofilechannels.DataGrid',
        'Mdc.view.setup.deviceloadprofilechannels.DataPreview'
    ],

    channel: null,
    router: null,

    emptyComponent: {
        xtype: 'no-items-found-panel',
        title: Uni.I18n.translate('deviceloadprofiles.data.empty.title', 'MDC', 'No readings found'),
        reasons: [
            Uni.I18n.translate('deviceloadprofiles.data.empty.list.item1', 'MDC', 'No readings have been defined yet.')
        ]
    },

    initComponent: function () {
        var me = this;

        me.grid = {
            xtype: 'deviceLoadProfileChannelDataGrid',
            channelRecord: this.channel,
            router: me.router
        };

        me.previewComponent = {
            xtype: 'deviceLoadProfileChannelDataPreview',
            channelRecord: this.channel
        };

        me.callParent(arguments);
    }
});