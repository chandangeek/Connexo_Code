Ext.define('Imt.channeldata.view.TableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.channelTableView',
    itemId: 'channelTableView',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.channeldata.view.DataGrid',
        'Imt.channeldata.view.DataPreview'
    ],

    channel: null,
    router: null,

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'ctr-table-no-data',
        title: Uni.I18n.translate('channels.data.empty.title', 'IMT', 'No readings found'),
        reasons: [
            Uni.I18n.translate('channels.data.empty.list.item1', 'IMT', 'No readings have been defined yet.')
        ]
    },

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'channelDataGrid',
            channelRecord: this.channel,
            router: me.router
        };

        me.previewComponent = {
            xtype: 'channelDataPreview',
            channelRecord: this.channel,
            router: me.router,
            hidden: true
        };

        me.callParent(arguments);
    }
});