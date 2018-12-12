/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.util.FilterStoreHydrator', {
    extract: function(filter) {
        var data = filter.getData();
        data.deviceGroups = data.deviceGroup;
        delete data.deviceGroup;

        // transform all single items int array
        _.map(data, function (item, key) {
            if (item) {
                if (!_.isArray(item)) {data[key] = [item]}
            }
            return item;
        });
        if (filter.startedBetween) {
            var start =  filter.getStartedBetween();
            if (start.get('from')) {
                data.startIntervalFrom = start.get('from').getTime();
            }

            if (start.get('to')) {
                data.startIntervalTo =  start.get('to').getTime();
            }
        }

        if (filter.finishedBetween) {
            var end = filter.getFinishedBetween();
            if (end.get('from')) {
                data.finishIntervalFrom = end.get('from').getTime();
            }

            if (end.get('to')) {
                data.finishIntervalTo = end.get('to').getTime();
            }
        }

        return data;
    }
});