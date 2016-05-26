Ext.define('Imt.channeldata.view.TableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.channelTableView',
    itemId: 'channelTableView',
    requires: [
        'Uni.util.FormEmptyMessage',
        'Imt.channeldata.view.DataGrid',
        'Imt.channeldata.view.DataPreview'
    ],

    channel: null,
    router: null,

    emptyComponent: {
        xtype: 'uni-form-empty-message',
        itemId: 'ctr-table-no-data',
        text: Uni.I18n.translate('channels.data.empty', 'IMT', 'No readings have been defined yet.')
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