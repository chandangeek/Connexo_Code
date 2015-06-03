Ext.define('Mdc.view.setup.deviceloadprofiles.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceLoadProfilesGrid',
    itemId: 'deviceLoadProfilesGrid',
    store: 'Mdc.store.LoadProfilesOfDevice',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.deviceloadprofiles.ActionMenu',
        'Mdc.store.TimeUnits'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('deviceloadprofiles.loadProfile', 'MDC', 'Load profile'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('devices/device/loadprofiles/loadprofiledata').buildUrl({mRID: encodeURIComponent(me.mRID), loadProfileId: record.get('id')});
                    return '<a href="{url}">{value}</a>'.replace('{url}', url).replace('{value}', Ext.String.htmlEncode(value));
                },
                flex: 1
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode',
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                dataIndex: 'interval_formatted',
                flex: 1
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
                    xtype: 'deviceLoadProfilesActionMenu',
                    itemId: 'loadProfileActionMenu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceloadprofiles.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} load profiles'),
                displayMoreMsg: Uni.I18n.translate('deviceloadprofiles.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} load profiles'),
                emptyMsg: Uni.I18n.translate('deviceloadprofiles.pagingtoolbartop.emptyMsg', 'MDC', 'There are no load profiles to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('deviceloadprofiles.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Load profiles per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});