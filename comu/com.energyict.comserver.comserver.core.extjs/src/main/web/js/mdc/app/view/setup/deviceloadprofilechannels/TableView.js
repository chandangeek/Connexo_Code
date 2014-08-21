Ext.define('Mdc.view.setup.deviceloadprofilechannels.TableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.deviceLoadProfileChannelTableView',
    itemId: 'deviceLoadProfileChannelTableView',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceloadprofilechannels.DataGrid',
        'Mdc.view.setup.deviceloadprofilechannels.DataPreview'
    ],

    grid: {
        xtype: 'deviceLoadProfileChannelDataGrid'
    },

    emptyComponent: {
        xtype: 'no-items-found-panel',
        title: Uni.I18n.translate('deviceloadprofiles.data.empty.title', 'MDC', 'No readings found'),
        reasons: [
            Uni.I18n.translate('deviceloadprofiles.data.empty.list.item1', 'MDC', 'No readings have been defined yet.')
        ]
    },

    previewComponent: {
        xtype: 'deviceLoadProfileChannelDataPreview'
    }
});