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
                    var url = me.router.getRoute('devices/device/loadprofiles/loadprofiledata').buildUrl({deviceId: encodeURIComponent(me.deviceId), loadProfileId: record.get('id')});
                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                dataIndex: 'interval_formatted',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.dataUntil', 'MDC', 'Data until'),
                dataIndex: 'dataUntil',
                renderer: function (value) {
                    return !Ext.isEmpty(value) ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
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