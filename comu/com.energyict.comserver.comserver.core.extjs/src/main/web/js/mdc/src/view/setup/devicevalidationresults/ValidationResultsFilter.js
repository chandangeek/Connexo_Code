Ext.define('Mdc.view.setup.devicevalidationresults.ValidationResultsFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    requires: [
        'Mdc.store.LoadProfilesOfDevice'
    ],
    store: 'ext-empty-store',
    alias: 'widget.mdc-device-validation-results-filter',
    duration: '1years',

    initComponent: function () {
        var me = this,
            intervalStart = (new Date()).setHours(0, 0, 0, 0);

        me.filters = [
            {
                type: 'datetimeselect',
                value: moment(intervalStart).toDate(),
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
        me.down('button[action=clearAll]').setDisabled(!((options.params.filter && Ext.decode(options.params.filter).length)));
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
    }
});