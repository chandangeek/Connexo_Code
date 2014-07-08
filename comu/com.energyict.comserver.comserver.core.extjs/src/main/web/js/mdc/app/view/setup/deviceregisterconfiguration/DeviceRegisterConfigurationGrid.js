Ext.define('Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationGrid', {
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
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationActionMenu'
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
                fixed: true,
                flex: 3
            },
            {
                xtype: 'actioncolumn',
                fixed: true,
                flex: 1,
                renderer: function (value, metaData, record) {
                    return '<div class="x-grid-cell-inner" style="float:left; font-size: 13px; line-height: 1em;">'
                        + record.getReadingType().get('mrid') + '&nbsp' + '&nbsp'
                        + '</div>';
                },
                header: Uni.I18n.translate('deviceregisterconfiguration.readingType', 'MDC', 'Reading type'),
                items: [
                    {
                        icon: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                        iconCls: 'uni-info-icon',
                        tooltip: Uni.I18n.translate('deviceregisterconfiguration.readingType.tooltip', 'MDC', 'Reading type info'),
                        handler: function (grid, rowIndex, colIndex, item, e) {
                            var record = grid.getStore().getAt(rowIndex);
                            this.fireEvent('showReadingTypeInfo', record);
                        }
                    }
                ]
            },
            {
                xtype: 'uni-actioncolumn',
                items:'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationActionMenu'
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

