Ext.define('Mdc.view.setup.devicechannels.TableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.deviceLoadProfileChannelTableView',
    itemId: 'deviceLoadProfileChannelTableView',
    requires: [
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.devicechannels.DataGrid',
        'Mdc.view.setup.devicechannels.DataPreview'
    ],

    channel: null,
    router: null,
    mentionDataLoggerSlave: false,

    emptyComponent: {
        xtype: 'uni-form-empty-message',
        itemId: 'ctr-table-no-data',
        text: Uni.I18n.translate('deviceloadprofiles.data.empty', 'MDC', 'No readings have been defined yet.')
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
            mentionDataLoggerSlave: me.mentionDataLoggerSlave,
            hidden: true
        };

        me.callParent(arguments);
    }
});