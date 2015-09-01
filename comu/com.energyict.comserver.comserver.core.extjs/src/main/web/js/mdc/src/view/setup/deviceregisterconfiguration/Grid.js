Ext.define('Mdc.view.setup.deviceregisterconfiguration.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceRegisterConfigurationGrid',
    itemId: 'deviceRegisterConfigurationGrid',
    mRID: null,
    store: 'RegisterConfigsOfDevice',
    scroll: false,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterConfigsOfDevice',
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu'
    ],
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true

    },
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.register', 'MDC', 'Register'),
                dataIndex: 'readingType',
                renderer: function (value, metaData, record) {
                    return '<a href="#/devices/' + me.mRID + '/registers/' + record.get('id') + '/data">' + Ext.String.htmlEncode(value.fullAliasName) + '</a>';
                },
                flex: 3
            },
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.timestampLastValue', 'MDC', 'Timestamp last value'),
                dataIndex: 'timeStamp',
                renderer: function(value){
                    if(!Ext.isEmpty(value)) {
                        return Uni.DateTime.formatDateTimeShort(new Date(value));
                    }
                    return '-';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.lastReading', 'MDC', 'Last reading'),
                dataIndex: 'reportedDateTime',
                renderer: function(value){
                    if(!Ext.isEmpty(value)) {
                        return Uni.DateTime.formatDateTimeShort(new Date(value));
                    }
                    return '-';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.lastValue', 'MDC', 'Last value'),
                dataIndex: 'value',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'deviceRegisterConfigurationActionMenu',
                    itemId: 'registerActionMenu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} registers'),
                displayMoreMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} registers'),
                emptyMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbartop.emptyMsg', 'MDC', 'There are no registers')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {mRID: me.mRID}
                ],
                itemsPerPageMsg: Uni.I18n.translate('devcieregisterconfiguration.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register configurations per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

