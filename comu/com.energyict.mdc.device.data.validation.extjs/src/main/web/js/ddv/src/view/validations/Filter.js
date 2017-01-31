/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.view.validations.Filter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'ddv-validations-filter',
    store: 'Ddv.store.Validations',
    filterDefault: {},
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'deviceGroups',
                emptyText: Uni.I18n.translate('validations.filter.deviceGroups', 'DDV', 'Device groups'),
                itemId: 'validations-topfilter-device-group',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                width: 181,
                store: 'Ddv.store.DeviceGroups'
            },
            {
                type: 'interval',
                dataIndex: 'between',
                dataIndexFrom: 'from',
                dataIndexTo: 'to',
                itemId: 'validations-topfilter-between',
                text: Uni.I18n.translate('validations.filter.period', 'DDV', 'Period'),
                defaultFromDate: me.filterDefault.from,
                defaultToDate: me.filterDefault.to
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfSuspects',
                itemId: 'validations-topfilter-amount-of-suspects',
                text: Uni.I18n.translate('validations.filter.amountOfSuspects', 'DDV', 'Amount of suspects')
            },
            {
                type: 'combobox',
                width: 181,
                dataIndex: 'validator',
                emptyText: Uni.I18n.translate('validations.filter.validator', 'DDV', 'Validator'),
                itemId: 'validations-topfilter-validator',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Ddv.store.Validators'
            }
        ];
        me.callParent(arguments);
    },

    enableClearAll: function (filters) {
        var me = this,
            enableClearAllBasedOnOtherThanFromTo = Ext.Array.filter(filters, function (filter) {
                return filter.property !== 'from' && filter.property !== 'to' && !Ext.Array.contains(me.noUiFilters, filter.property);
            }).length > 0,
            fromFilter = _.find(filters, function (item) {
                return item.property === 'from';
            }),
            toFilter = _.find(filters, function (item) {
                return item.property === 'to';
            }),
            fromToFilterIsDefault = fromFilter && me.filterDefault.from && fromFilter.value === me.filterDefault.from.getTime() &&
                toFilter && me.filterDefault.to && toFilter.value === me.filterDefault.to.getTime();

        Ext.suspendLayouts();
        me.down('button[action=clearAll]').setDisabled(enableClearAllBasedOnOtherThanFromTo ? false : fromToFilterIsDefault);
        Ext.resumeLayouts(true);
    }
});