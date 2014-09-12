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

    emptyComponent: {
        xtype: 'no-items-found-panel',
        title: Uni.I18n.translate('deviceloadprofiles.data.empty.title', 'MDC', 'No readings found'),
        reasons: [
            Uni.I18n.translate('deviceloadprofiles.data.empty.list.item1', 'MDC', 'No readings have been defined yet.')
        ]
    },

    initComponent: function () {
        this.grid = {
            xtype: 'deviceLoadProfileChannelDataGrid',
            channelRecord: this.channel
        };

        this.previewComponent = {
            xtype: 'deviceLoadProfileChannelDataPreview',
            channelRecord: this.channel
        };

        this.callParent(arguments);

    }
});