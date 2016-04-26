Ext.define('Scs.view.ServiceCallFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    store: 'Scs.store.ServiceCalls',
    alias: 'widget.service-call-filter',
    modDateHidden: false,
    filterDefault: {},

    stores: [
        'Scs.store.ServiceCallTypes',
        'Scs.store.States'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'text',
                dataIndex: 'name',
                emptyText: Uni.I18n.translate('general.IDOrReference', 'SCS', 'ID or reference')
            },
            {
                type: 'combobox',
                dataIndex: 'type',
                emptyText: Uni.I18n.translate('general.type', 'SCS', 'Type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'name',
                store: 'Scs.store.ServiceCallTypes'
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'SCS', 'Status'),
                multiSelect: true,
                displayField: 'displayValue',
                valueField: 'id',
                store: 'Scs.store.States'
            },
            {
                type: 'interval',
                dataIndex: 'creationTime',
                dataIndexFrom: 'receivedDateFrom',
                dataIndexTo: 'receivedDateTo',
                defaultFromDate: me.filterDefault.fromDate,
                defaultToDate: me.filterDefault.toDate,
                text: Uni.I18n.translate('general.receivedDate', 'SCS', 'Received date')
            },
            {
                type: 'interval',
                dataIndex: 'lastModificationTime',
                dataIndexFrom: 'modificationDateFrom',
                dataIndexTo: 'modificationDateTo',
                text: Uni.I18n.translate('general.modificationDate', 'SCS', 'Modification date'),
                hidden: me.modDateHidden
            }
        ];

        me.callParent(arguments);
    }
});