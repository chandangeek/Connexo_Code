/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                itemId: 'service-call-name-filter',
                dataIndex: 'name',
                emptyText: Uni.I18n.translate('general.IDOrReference', 'SCS', 'ID or reference')
            },
            {
                type: 'combobox',
                itemId: 'service-call-type-filter',
                dataIndex: 'type',
                emptyText: Uni.I18n.translate('general.type', 'SCS', 'Type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'name',
                store: 'Scs.store.ServiceCallTypes'
            },
            {
                type: 'combobox',
                itemId: 'service-call-status-filter',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'SCS', 'Status'),
                multiSelect: true,
                displayField: 'displayValue',
                valueField: 'id',
                store: 'Scs.store.States'
            },
            {
                type: 'interval',
                itemId: 'service-call-received-date-filter',
                dataIndex: 'creationTime',
                dataIndexFrom: 'receivedDateFrom',
                dataIndexTo: 'receivedDateTo',
                defaultFromDate: me.filterDefault.fromDate,
                defaultToDate: me.filterDefault.toDate,
                text: Uni.I18n.translate('general.receivedDate', 'SCS', 'Received date')
            },
            {
                type: 'interval',
                itemId: 'service-call-modification-date-filter',
                dataIndex: 'lastModificationTime',
                dataIndexFrom: 'modificationDateFrom',
                dataIndexTo: 'modificationDateTo',
                text: Uni.I18n.translate('general.modificationDate', 'SCS', 'Modification date'),
                hidden: me.modDateHidden
            }
        ];

        me.callParent(arguments);
    },

    enableClearAll: function (filters) {
        var me = this,
            enableClearAllBasedOnOtherThanDates = Ext.Array.filter(filters, function (filter) {
                    return filter.property !== 'receivedDateFrom' && filter.property !== 'receivedDateTo' &&
                           filter.property !== 'modificationDateFrom' && filter.property !== 'modificationDateTo' &&
                           !Ext.Array.contains(me.noUiFilters, filter.property);
                }).length > 0,
            receivedDateFromFilter = _.find(filters, function (item) {
                return item.property === 'receivedDateFrom';
            }),
            receivedDateToFilter = _.find(filters, function (item) {
                return item.property === 'receivedDateTo';
            }),
            modDateFromFilter = _.find(filters, function (item) {
                return item.property === 'modificationDateFrom';
            }),
            modDateToFilter = _.find(filters, function (item) {
                return item.property === 'modificationDateTo';
            }),
            receivedDateFilterIsDefault =
                ( Ext.isEmpty(receivedDateFromFilter) && Ext.isEmpty(me.filterDefault.fromDate) && Ext.isEmpty(receivedDateToFilter) && Ext.isEmpty(me.filterDefault.toDate) )
                ||
                ( !Ext.isEmpty(receivedDateFromFilter) && !Ext.isEmpty(me.filterDefault.fromDate) &&
                     receivedDateFromFilter.value === me.filterDefault.fromDate.getTime() &&
                     !Ext.isEmpty(receivedDateToFilter) && !Ext.isEmpty(me.filterDefault.toDate) &&
                     receivedDateToFilter.value === me.filterDefault.toDate.getTime()
                ),
            modDateFilterIsDefault =
                ( Ext.isEmpty(modDateFromFilter) && Ext.isEmpty(modDateToFilter) )
                ||
                false, // So far, no default for modification date is known
            enableClearAllBasedOnDates = !receivedDateFilterIsDefault;

        if (!me.modDateHidden && !enableClearAllBasedOnDates) {
            enableClearAllBasedOnDates = !modDateFilterIsDefault;
        }
        Ext.suspendLayouts();
        me.down('button[action=clearAll]').setDisabled(enableClearAllBasedOnOtherThanDates ? false : !enableClearAllBasedOnDates);
        Ext.resumeLayouts(true);
    }

});