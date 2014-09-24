Ext.define('Dsh.util.FilterStoreHydrator', {
    extract: function(filter) {

        var data = filter.getData();

        // transform all single items int array
        _.map(data, function (item) {
            if (item) {
                if (!_.isArray(item)) {item = [item]}
            }
        });

        if (filter.startedBetween) {
            var start =  filter.getStartedBetween();
            if (start.get('from')) {
                data.startedBetween = {property: 'startIntervalFrom', value: start.get('from').getTime()};
            }

            if (start.get('to')) {
                data.finishedBetween = {property: 'startIntervalTo', value: start.get('to').getTime()};
            }
        }

        if (filter.finishedBetween) {
            var end = filter.getFinishedBetween();
            if (end.get('from')) {
                data.finishedBetween = {property: 'finishIntervalFrom', value: end.get('from').getTime()};
            }

            if (end.get('to')) {
                data.finishedBetween = {property: 'finishIntervalTo', value: end.get('to').getTime()};
            }
        }

        return data;
    }
});