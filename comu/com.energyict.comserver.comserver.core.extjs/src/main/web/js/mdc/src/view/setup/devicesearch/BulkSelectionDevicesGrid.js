Ext.define('Mdc.view.setup.devicesearch.BulkSelectionDevicesGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'bulk-selection-mdc-search-results-grid',
    overflowY: 'auto',

    requires: [
        'Mdc.store.Devices',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.grid.BulkSelection',
        'Mdc.store.DevicesBuffered'
    ],

    store: 'Mdc.store.DevicesBuffered',

    bottomToolbarHidden: true,

    initComponent: function () {
        var me = this;

        me.store = Ext.getStore(me.store) || Ext.create(me.store);

        me.columns = [
            {
                header: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID'),
                dataIndex: 'mRID',
                sortable: false,
                hideable: false,
                renderer: function (value, b, record) {
                    return '<a href="#/devices/' + record.get('mRID') + '">' + value + '</a>';
                },
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number'),
                dataIndex: 'serialNumber',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('searchItems.type', 'MDC', 'Type'),
                dataIndex: 'deviceTypeName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('searchItems.configuration', 'MDC', 'Configuration'),
                dataIndex: 'deviceConfigurationName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            }
        ];

        me.callParent();
    }
});
