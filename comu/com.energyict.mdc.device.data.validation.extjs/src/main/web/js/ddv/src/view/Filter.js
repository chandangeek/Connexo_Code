/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.view.Filter', {
    extend: 'Uni.grid.FilterPanelTop',
    requires: [
        'Ddv.store.DeviceGroups',
        'Ddv.store.Validators',
        'Ddv.store.Estimators',
        'Ddv.store.DeviceTypes'
    ],
    xtype: 'ddv-quality-filter',
    store: 'Ddv.store.DataQuality',
    filterDefault: {},
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'deviceGroup',
                emptyText: Uni.I18n.translate('general.deviceGroup', 'DDV', 'Device group'),
                itemId: 'ddv-filter-device-group',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Ddv.store.DeviceGroups'
            },
            {
                type: 'combobox',
                dataIndex: 'deviceType',
                emptyText: Uni.I18n.translate('general.deviceType', 'DDV', 'Device type'),
                itemId: 'ddv-filter-device-type',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Ddv.store.DeviceTypes'
            },
            {
                type: 'interval',
                dataIndex: 'between',
                dataIndexFrom: 'from',
                dataIndexTo: 'to',
                itemId: 'ddv-filter-between',
                text: Uni.I18n.translate('general.period', 'DDV', 'Period'),
                defaultFromDate: me.filterDefault.from,
                defaultToDate: me.filterDefault.to,
                withoutTime: true
            },
            {
                type: 'readingquality',
                dataIndex: 'readingQuality',
                emptyText: Uni.I18n.translate('general.readingQuality', 'DDV', 'Reading quality'),
                itemId: 'ddv-filter-reading-quality'
            },
            {
                type: 'combobox',
                dataIndex: 'validator',
                emptyText: Uni.I18n.translate('general.validator', 'DDV', 'Validator'),
                itemId: 'ddv-filter-validator',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Ddv.store.Validators'
            },
            {
                type: 'combobox',
                dataIndex: 'estimator',
                emptyText: Uni.I18n.translate('general.estimator', 'DDV', 'Estimator'),
                itemId: 'ddv-filter-estimator',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Ddv.store.Estimators'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfSuspects',
                itemId: 'ddv-filter-amount-of-suspects',
                text: Uni.I18n.translate('general.amountOfSuspects', 'DDV', 'Amount of suspects') + '<span class="white-circle-filter-btn icon-flag5" style="color:red;"></span>'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfConfirmed',
                itemId: 'ddv-filter-amount-of-confirmed',
                text: Uni.I18n.translate('general.amountOfConfirmed', 'DDV', 'Amount of confirmed') + '<span class="white-circle-filter-btn icon-checkmark" style="color:#686868"></span>'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfEstimates',
                itemId: 'ddv-filter-amount-of-estimates',
                text: Uni.I18n.translate('general.amountOfEstimates', 'DDV', 'Amount of estimates') + '<span class="white-circle-filter-btn icon-flag5" style="color:#33CC33"></span>'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfInformatives',
                itemId: 'ddv-filter-amount-of-informatives',
                text: Uni.I18n.translate('general.amountOfInformatives', 'DDV', 'Amount of informatives') + '<span class="white-circle-filter-btn icon-flag5" style="color:#dedc49"></span>'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfEdited',
                itemId: 'ddv-filter-amount-of-edited',
                text: Uni.I18n.translate('general.amountOfEdited', 'DDV', 'Amount of edited') + '<span class="white-circle-filter-btn icon-pencil4" style="color:#686868"></span>'
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