Ext.define('Mdc.view.setup.deviceloadprofiles.DataGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfilesDataGrid',
    itemId: 'deviceLoadProfilesDataGrid',
    store: 'Mdc.store.LoadProfilesOfDeviceData',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.deviceloadprofiles.DataActionMenu',
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    height: 395,
    plugins: [
        'bufferedrenderer',
        'showConditionalToolTip'
    ],
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
                header: channel.name,
                dataIndex: 'channelData',
                minWidth : 300,
                flex: 1,
                renderer: function (data) {
                    return data[channel.id] ? data[channel.id] + ' ' + channel.unitOfMeasure.localizedValue : '';
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