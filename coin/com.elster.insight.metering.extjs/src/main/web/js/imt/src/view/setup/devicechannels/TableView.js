Ext.define('Imt.view.setup.devicechannels.TableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.deviceLoadProfileChannelTableView',
    itemId: 'deviceLoadProfileChannelTableView',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.view.setup.devicechannels.DataGrid',
        'Imt.view.setup.devicechannels.DataPreview'
    ],

    channel: null,
    router: null,

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'ctr-table-no-data',
        title: Uni.I18n.translate('deviceloadprofiles.data.empty.title', 'IMT', 'No readings found'),
        reasons: [
            Uni.I18n.translate('deviceloadprofiles.data.empty.list.item1', 'IMT', 'No readings have been defined yet.')
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
            channelRecord: this.channel,
            router: me.router,
            hidden: true
        };

        me.callParent(arguments);
    }
});