Ext.define('Mdc.view.setup.deviceloadprofilechannels.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelDataGrid',
    itemId: 'deviceLoadProfileChannelDataGrid',
    store: 'Mdc.store.ChannelOfLoadProfileOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Mdc.view.setup.deviceloadprofilechannels.DataActionMenu'
    ],
    height: 395,
    plugins: {
        ptype: 'bufferedrenderer'
    },
    columns: [
        {
            header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
            dataIndex: 'interval_end',
            width: 200
        },
        {
            header: Uni.I18n.translate('deviceloadprofiles.channels.value', 'MDC', 'Value'),
            dataIndex: 'value',
            flex: 1
        },
        {
            header: Uni.I18n.translate('deviceloadprofiles.multiplier', 'MDC', 'Multiplier'),
            dataIndex: 'multiplier',
            flex: 1
        },
        {
            xtype: 'uni-actioncolumn',
            menu: {
                xtype: 'deviceLoadProfileChannelDataActionMenu'
            }
        }
    ]
});