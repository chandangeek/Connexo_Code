/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dataquality.view.Filter', {
    extend: 'Uni.grid.FilterPanelTop',
    requires: [
        'Imt.dataquality.store.UsagePointGroups',
        'Imt.dataquality.store.Validators',
        'Imt.dataquality.store.Estimators',
        'Imt.dataquality.store.MetrologyConfigurations',
        'Imt.dataquality.store.Purposes',
        'Imt.dataquality.store.ReadingQuality'
    ],
    xtype: 'imt-quality-filter',
    store: 'Imt.dataquality.store.DataQuality',
    filterDefault: {},
    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'usagePointGroup',
                emptyText: Uni.I18n.translate('general.usagePointGroup', 'IMT', 'Usage point group'),
                itemId: 'imt-filter-usage-point-group',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Imt.dataquality.store.UsagePointGroups'
            },
            {
                type: 'combobox',
                dataIndex: 'metrologyConfiguration',
                emptyText: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                itemId: 'imt-filter-metrology-config',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Imt.dataquality.store.MetrologyConfigurations'
            },
            {
                type: 'combobox',
                dataIndex: 'metrologyPurpose',
                emptyText: Uni.I18n.translate('general.purpose', 'IMT', 'Purpose'),
                itemId: 'imt-filter-purpose',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Imt.dataquality.store.Purposes'
            },
            {
                type: 'interval',
                dataIndex: 'between',
                dataIndexFrom: 'from',
                dataIndexTo: 'to',
                itemId: 'imt-filter-between',
                text: Uni.I18n.translate('general.period', 'IMT', 'Period'),
                defaultFromDate: me.filterDefault.from,
                defaultToDate: me.filterDefault.to,
                withoutTime: true,
                minValue: moment().subtract(3, 'months').toDate()
            },
            {
                type: 'readingquality',
                store: 'Imt.dataquality.store.ReadingQuality',
                dataIndex: 'readingQuality',
                emptyText: Uni.I18n.translate('general.readingQuality', 'IMT', 'Reading quality'),
                itemId: 'imt-filter-reading-quality'
            },
            {
                type: 'combobox',
                dataIndex: 'validator',
                emptyText: Uni.I18n.translate('general.validator', 'IMT', 'Validator'),
                itemId: 'imt-filter-validator',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Imt.dataquality.store.Validators'
            },
            {
                type: 'combobox',
                dataIndex: 'estimator',
                emptyText: Uni.I18n.translate('general.estimator', 'IMT', 'Estimator'),
                itemId: 'imt-filter-estimator',
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Imt.dataquality.store.Estimators'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfSuspects',
                itemId: 'imt-filter-amount-of-suspects',
                text: Uni.I18n.translate('general.amountOfSuspects', 'IMT', 'Amount of suspects') + '<span class="white-circle-filter-btn icon-flag5" style="color:red;"></span>'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfConfirmed',
                itemId: 'imt-filter-amount-of-confirmed',
                text: Uni.I18n.translate('general.amountOfConfirmed', 'IMT', 'Amount of confirmed') + '<span class="white-circle-filter-btn icon-checkmark" style="color:#686868"></span>'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfEstimates',
                itemId: 'imt-filter-amount-of-estimates',
                text: Uni.I18n.translate('general.amountOfEstimates', 'IMT', 'Amount of estimates') + '<span class="white-circle-filter-btn icon-flag5" style="color:#33CC33"></span>'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfInformatives',
                itemId: 'imt-filter-amount-of-informatives',
                text: Uni.I18n.translate('general.amountOfInformatives', 'IMT', 'Amount of informatives') + '<span class="white-circle-filter-btn icon-flag5" style="color:#dedc49"></span>'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfEdited',
                itemId: 'imt-filter-amount-of-edited',
                text: Uni.I18n.translate('general.amountOfEdited', 'IMT', 'Amount of edited') + '<span class="white-circle-filter-btn icon-pencil4" style="color:#686868"></span>'
            },
            {
                type: 'numeric',
                dataIndex: 'amountOfProjected',
                itemId: 'imt-filter-amount-of-projected',
                text: Uni.I18n.translate('general.amountOfProjected', 'IMT', 'Amount of projected') + '<span class="white-circle-filter-btn" style="font-weight:bold; cursor: default; font-size: 7px; color:#686868">  P  </span>'
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