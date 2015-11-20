Ext.define('Imt.channeldata.view.TableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.channelTableView',
    itemId: 'channelTableView',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.channeldata.view.DataGrid',
        'Imt.channeldata.view.DataPreview'
    ],

    channelRecord: null,
    router: null,

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'ctr-table-no-data',
        title: Uni.I18n.translate('channel.data.empty.title', 'IMT', 'No readings found'),
        reasons: [
            Uni.I18n.translate('channel.data.empty.list.item1', 'IMT', 'No readings have been defined yet.')
        ]
    },

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'channel-data-grid',
            channelRecord: this.channelRecord,
            router: me.router
        };

        me.previewComponent = {
            xtype: 'channelDataPreview',
            channelRecord: this.channelRecord,
            router: me.router,
            hidden: true
        };

        me.callParent(arguments);
    }
});