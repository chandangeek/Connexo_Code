Ext.define('Imt.view.setup.devicechannels.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelsGrid',
    itemId: 'deviceLoadProfileChannelsGrid',
    store: 'Imt.store.ChannelsOfLoadProfilesOfDevice',

    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.view.setup.devicechannels.ActionMenu'
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
                    return me.router.getRoute('usagepoints/view/channels/channel').buildUrl({
                        mRID: me.mRID,
                        channelId: record.get('readingType').mRID
                    });
                }
            },
            {
                dataIndex: 'interval',
                flex: 1,
                header: Uni.I18n.translate('devicechannels.interval', 'IMT', 'Interval'),
                renderer: function (value) {
                    var res = '';
                    value ? res = Ext.String.htmlEncode('{count} {timeUnit}'.replace('{count}', value.count).replace('{timeUnit}', value.timeUnit)) : null;
                    return res
                }
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.lastReading', 'IMT', 'Last reading'),
                dataIndex: 'lastValueTimestamp',
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
                displayMsg: Uni.I18n.translate('devicechannels.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} channels'),
                displayMoreMsg: Uni.I18n.translate('devicechannels.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} channels'),
                emptyMsg: Uni.I18n.translate('devicechannels.pagingtoolbartop.emptyMsg', 'IMT', 'There are no channels to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('devicechannels.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Channels per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});