Ext.define('Dsh.store.ConnectionTasks', {
    extend: 'Ext.data.Store',
    requires: [
        'Dsh.model.ConnectionTask'
    ],
    model: 'Dsh.model.ConnectionTask',
    remoteFilter: true,
    autoLoad: false,
    sorters: [
        {
            direction: 'DESC',
            property: 'startDateTime'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dsr/connections',
        reader: {
            type: 'json',
            root: 'connectionTasks',
            totalProperty: 'total'
        }
    },

    setFilterModel: function(filterModel) {
        var me = this,
            data = filterModel.getData(),
            filters = [];

        _.map(data, function (item, key) {
            if (item) {
                if (!_.isArray(item)) {item = [item]}
                filters.push({property: key, value: item});
            }
        });

        if (filterModel.startedBetween) {
            var start =  filterModel.getStartedBetween();

            if (start.get('from')) {
                filters.push({property: 'startIntervalFrom', value: start.get('from').getTime()});
            }

            if (start.get('to')) {
                filters.push({property: 'startIntervalTo', value: start.get('to').getTime()});
            }
        }

        if (filterModel.finishedBetween) {
            var end = filterModel.getFinishedBetween();
            if (end.get('from')) {
                filters.push({property: 'finishIntervalFrom', value: end.get('from').getTime()});
            }

            if (end.get('to')) {
                filters.push({property: 'finishIntervalTo', value: end.get('to').getTime()});
            }
        }

        me.clearFilter();
        me.addFilter(filters);
    }
});

