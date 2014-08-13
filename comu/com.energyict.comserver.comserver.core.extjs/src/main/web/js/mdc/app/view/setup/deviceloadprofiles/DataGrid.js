Ext.define('Mdc.view.setup.deviceloadprofiles.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfilesDataGrid',
    itemId: 'deviceLoadProfilesDataGrid',
    store: 'Mdc.store.LoadProfilesOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.deviceloadprofiles.DataActionMenu'
    ],
    height: 395,
    plugins: {
        ptype: 'bufferedrenderer'
    },
    channels: null,
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                dataIndex: 'interval_end',
                width: 200
            }
        ];
        Ext.Array.each(me.channels, function (channel) {
            me.columns.push({
                header: channel.name + ' ' + channel.unitOfMeasure.localizedValue,
                dataIndex: 'channelData',
                flex: 1,
                renderer: function (data) {
                    return data[channel.id];
                }
            });
        });
        me.columns.push({
            xtype: 'uni-actioncolumn',
            menu: {
                xtype: 'deviceLoadProfilesDataActionMenu'
            }
        });

        me.callParent(arguments);
    }
});