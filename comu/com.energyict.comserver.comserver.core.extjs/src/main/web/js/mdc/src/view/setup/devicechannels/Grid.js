Ext.define('Mdc.view.setup.devicechannels.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelsGrid',
    itemId: 'deviceLoadProfileChannelsGrid',
    store: 'Mdc.store.ChannelsOfLoadProfilesOfDevice',

    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.devicechannels.ActionMenu'
    ],

    mRID: null,
    router: null,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType',
                flex: 2,
                showTimeAttribute: false,
                makeLink: function (record) {
                    return me.router.getRoute('devices/device/channels/channeldata').buildUrl({
                        mRID: me.mRID,
                        channelId: record.getId()
                    });
                }
            },
            {
                dataIndex: 'interval',
                flex: 1,
                header: Uni.I18n.translate('devicechannels.interval', 'MDC', 'Interval'),
                renderer: function (value) {
                    var res = '';
                    value ? res = Ext.String.htmlEncode('{count} {timeUnit}'.replace('{count}', value.count).replace('{timeUnit}', value.timeUnit)) : null;
                    return res
                }
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.lastReading', 'MDC', 'Last reading'),
                dataIndex: 'lastReading',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'deviceLoadProfileChannelsActionMenu',
                    itemId: 'channelActionMenu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                usesExactCount: true,
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devicechannels.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} channels'),
                displayMoreMsg: Uni.I18n.translate('devicechannels.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} channels'),
                emptyMsg: Uni.I18n.translate('devicechannels.pagingtoolbartop.emptyMsg', 'MDC', 'There are no channels to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('devicechannels.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Channels per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});