Ext.define('Mdc.view.setup.deviceloadprofilechannels.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfileChannelsGrid',
    itemId: 'deviceLoadProfileChannelsGrid',
    store: 'Mdc.store.ChannelsOfLoadProfilesOfDevice',
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.deviceloadprofilechannels.ActionMenu'
    ],

    mRID: null,
    router: null,
    loadProfileId: null,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.channel', 'MDC', 'Channel'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('devices/device/loadprofiles/loadprofile/channels/channel/data').buildUrl({mRID: me.mRID, loadProfileId: me.loadProfileId, channelId: record.getId()});
                    return '<a href="' + url + '">' + value + '</a>';
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.unitOfMeasure', 'MDC', 'Unit of measure'),
                dataIndex: 'unitOfMeasure_formatted',
                flex: 2
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'deviceLoadProfileChannelsActionMenu'
                },
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceloadprofilechannels.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} channels'),
                displayMoreMsg: Uni.I18n.translate('deviceloadprofilechannels.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} channels'),
                emptyMsg: Uni.I18n.translate('deviceloadprofilechannels.pagingtoolbartop.emptyMsg', 'MDC', 'There are no channels to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('deviceloadprofilechannels.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Channels per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});