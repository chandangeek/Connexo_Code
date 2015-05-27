Ext.define('Mdc.view.setup.devicesearch.DevicesGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'mdc-search-results-grid',
    overflowY: 'auto',

    requires: [
        'Mdc.store.Devices',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.grid.BulkSelection',
        'Mdc.store.DevicesBuffered'
    ],

    selModel: {
        mode: 'SINGLE'
    },

    store: 'Mdc.store.Devices',

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
                    return '<a href="#/devices/' + encodeURIComponent(record.get('mRID')) + '">' + Ext.String.htmlEncode(value) + '</a>';
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

        me.dockedItems = [
         {
         xtype: 'pagingtoolbartop',
         itemId: 'searchItemsToolbarTop',
         store: me.store,
         dock: 'top',
         displayMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} devices'),
         displayMoreMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} devices'),
         emptyMsg: Uni.I18n.translate('devices.pagingtoolbartop.emptyMsg', 'MDC', 'There are no devices to display')
         },
         {
         xtype: 'pagingtoolbarbottom',
         itemId: 'searchItemsToolbarBottom',
         store: me.store,
         dock: 'bottom',
         itemsPerPageMsg: Uni.I18n.translate('devices.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Devices per page')
         }
         ];

        me.callParent();
    }
});