/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    requires: [
        'Mdc.store.LoadProfilesOfDevice'
    ],
    store: 'ext-empty-store',
    alias: 'widget.mdc-device-validation-results-filter',
    duration: '1years',
    todayMidnight: moment((new Date()).setHours(0, 0, 0, 0)).toDate(),

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'datetimeselect',
                value: me.todayMidnight,
                dataIndex: 'intervalStart',
                name: 'intervalStart',
                fireFilterUpdateEvent: function () {
                    // (CXO-2787) Don't fire, hence the new filter value won't be applied until you press the main "Apply" button
                }
            }

        ];

        me.callParent(arguments);
    },

    clearFilters: function () {
        var me = this,
            intervalStart = (new Date()).setHours(0, 0, 0, 0);

        me.down('uni-grid-filtertop-datetime-select').setFilterValue(intervalStart);
        me.applyFilters();
    },

    onBeforeLoad: function (store, options) {
        var me = this,
            params = {};

        options.params = options.params || {};

        // Memory proxy.
        if (me.hasActiveFilter()) {
            var tempParams = {};

            Ext.merge(tempParams, me.getIntervalLoadProfileParam());
            Ext.merge(tempParams, me.getIntervalRegisterParam());
            if (me.filterObjectEnabled) {
                params[me.filterObjectParam] = me.createFiltersObject(tempParams);
            } else {
                params = tempParams;
            }
        }

        if (me.historyEnabled) {
            me.updateHistoryState();
        }

        Ext.apply(options.params, params);
        me.enableClearAll(Ext.decode(options.params.filter, true) || []);
    },

    getIntervalLoadProfileParam: function () {
        var me = this,
            zoomLevelsStore = Ext.getStore('Uni.store.DataIntervalAndZoomLevels'),
            loadProfileStore = Ext.getStore('Mdc.store.LoadProfilesOfDevice'),
            filterParams = me.getFilterParams(false, !me.filterObjectEnabled),
            result = [];

        loadProfileStore.each(function (record) {
            result.push({
                id: record.getId(),
                intervalStart: filterParams.intervalStart,
                intervalEnd: filterParams.intervalStart + zoomLevelsStore.getIntervalInMs(zoomLevelsStore.getIntervalRecord(record.get('interval')).get('all'))
            });
        });

        return {
            intervalLoadProfile: result
        };
    },

    getIntervalRegisterParam: function () {
        var me = this,
            durationStore = Ext.getStore('Mdc.store.ValidationResultsDurations'),
            filterParams = me.getFilterParams(false, !me.filterObjectEnabled),
            duration = durationStore.getById(me.duration);

        return {
            intervalRegisterStart: filterParams.intervalStart,
            intervalRegisterEnd: moment(filterParams.intervalStart).add(duration.get('timeUnit'), duration.get('count')).valueOf()
        };
    },

    enableClearAll: function (filters) {
        var me = this,
            fromFilter = _.find(filters, function (item) {
                return item.property === 'intervalLoadProfile';
            }),
            fromFilterIsDefault = fromFilter && Ext.isArray(fromFilter.value) && fromFilter.value[0].intervalStart === me.todayMidnight.getTime();

        Ext.suspendLayouts();
        me.down('button[action=clearAll]').setDisabled(fromFilterIsDefault);
        Ext.resumeLayouts(true);
    }

});