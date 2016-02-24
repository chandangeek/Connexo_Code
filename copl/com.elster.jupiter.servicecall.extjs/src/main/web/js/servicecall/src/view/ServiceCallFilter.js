Ext.define('Scs.view.ServiceCallFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    store: 'Scs.store.ServiceCalls',
    alias: 'widget.service-call-filter',

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'text',
                dataIndex: 'number',
                emptyText: Uni.I18n.translate('general.ID', 'SCS', 'ID')
            },
            {
                type: 'combobox',
                dataIndex: 'type',
                emptyText: Uni.I18n.translate('general.type', 'SCS', 'Type'),
                multiSelect: true,
                displayField: 'type',
                valueField: 'type',
                store: 'Scs.store.ServiceCalls'
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'SCS', 'Status'),
                multiSelect: true,
                displayField: 'state',
                valueField: 'state',
                store: 'Scs.store.ServiceCalls'
                //   hidden: !me.includeServiceCombo
            },
            {
                type: 'interval',
                dataIndex: 'creationTime',
                dataIndexFrom: 'receivedDateFrom',
                dataIndexTo: 'receivedDateTo',
                text: Uni.I18n.translate('general.receivedDate', 'SCS', 'Received date')
            },
            {
                type: 'interval',
                dataIndex: 'lastModificationTime',
                dataIndexFrom: 'modificationDateFrom',
                dataIndexTo: 'modificationDateTo',
                text: Uni.I18n.translate('general.modificationDate', 'SCS', 'Modification date')
            }
        ];

        me.callParent(arguments);
    }
});