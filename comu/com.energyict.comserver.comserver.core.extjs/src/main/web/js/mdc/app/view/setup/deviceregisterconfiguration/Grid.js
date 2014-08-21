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
        style: { overflow: 'auto', overflowX: 'hidden' }
    },
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.register', 'MDC', 'Register'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    return '<a href="#/devices/' + me.mRID + '/registers/' + record.get('id') + '">' + value + '</a>';
                },
                flex: 3
            },
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.latestMeasurement', 'MDC', 'Latest measurement'),
                xtype: 'datecolumn',
                format: 'M j, Y \\a\\t G:i',
                dataIndex: 'lastReading',
                defaultRenderer: function(value){
                    if(!Ext.isEmpty(value.timeStamp)) {
                        return Ext.util.Format.date(new Date(value.timeStamp), this.format);
                    }
                    return Uni.I18n.translate('deviceregisterconfiguration.latestMeasurement.notspecified', 'MDC', '-');
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.latestReading', 'MDC', 'Latest reading'),
                xtype: 'datecolumn',
                format: 'M j, Y \\a\\t G:i',
                dataIndex: 'lastReading',
                defaultRenderer: function(value){
                    if(!Ext.isEmpty(value.reportedDateTime)) {
                        return Ext.util.Format.date(new Date(value.reportedDateTime), this.format);
                    }
                    return Uni.I18n.translate('deviceregisterconfiguration.latestReading.notspecified', 'MDC', '-');
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceregisterconfiguration.latestValue', 'MDC', 'Latest value'),
                dataIndex: 'value',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items:'Mdc.view.setup.deviceregisterconfiguration.ActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} registers'),
                displayMoreMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} registers'),
                emptyMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbartop.emptyMsg', 'MDC', 'There are no registers to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {mRID: me.mRID}
                ],
                itemsPerPageMsg: Uni.I18n.translate('deviceregisterconfiguration.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Registers per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

