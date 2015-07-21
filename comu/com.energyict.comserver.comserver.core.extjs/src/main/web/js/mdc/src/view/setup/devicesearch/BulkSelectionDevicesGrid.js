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

    allLabel: Uni.I18n.translate('searchItems.BulkSelection.allLabel', 'MDC', 'All devices'),

    allDescription: Uni.I18n.translate(
        'searchItems.BulkSelection.allDescription',
        'MDC',
        'Select all devices (according to filter)'
    ),
    selectedLabel: Uni.I18n.translate('searchItems.BulkSelection.selectedLabel', 'MDC', 'Selected devices'),

    selectedDescription: Uni.I18n.translate(
        'searchItems.BulkSelection.selectedDescription',
        'MDC',
        'Select devices in table'
    ),

    allChosenByDefault: false,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'setup.searchitems.bulk.DevicesSelectionGrid.counterText',
            count,
            'MDC',
            '{0} devices selected'
        );
    },

    initComponent: function () {
        var me = this;

        //me.store = Ext.getStore(me.store) || Ext.create(me.store);

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

        me.callParent();
    },

    // specific method for a static devices grid on an edit device group page that is used instead of onSelectionChange()
    onSelectionChangeInGroup: function (records) {
        this.getSelectionCounter().setText(this.counterTextFn(records.length));
        this.getUncheckAllButton().setDisabled(!records.length);
    }
});
