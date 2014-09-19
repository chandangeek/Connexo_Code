Ext.define('Dsh.model.Filter', {
    extend: 'Uni.data.model.Filter',
    requires: [
        'Uni.data.proxy.QueryStringProxy',
        'Dsh.model.DateRange'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'currentStates', type: 'auto' },
        { name: 'latestStatus', type: 'auto' },
        { name: 'latestResults', type: 'auto' },
        { name: 'comPortPools', type: 'auto' },
        { name: 'comSchedules', type: 'auto' },
        { name: 'comTasks', type: 'auto' },
        { name: 'connectionTypes', type: 'auto' },
        { name: 'deviceTypes', type: 'auto' }
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'startedBetween',
            instanceName: 'startedBetween',
            associationKey: 'startedBetween',
            getterName: 'getStartedBetween',
            setterName: 'setStartedBetween'
        },
        {
            type: 'hasOne',
            model: 'Dsh.model.DateRange',
            name: 'finishedBetween',
            instanceName: 'finishedBetween',
            associationKey: 'finishedBetween',
            getterName: 'getFinishedBetween',
            setterName: 'setFinishedBetween'
        }
    ],

    getFilterData: function() {
       var data = this.callParent(arguments);

        if (this.startedBetween) {
            var start =  this.getStartedBetween();

            if (start.get('from')) {
                data.push({property: 'startIntervalFrom', value: start.get('from').getTime()});
            }

            if (start.get('to')) {
                data.push({property: 'startIntervalTo', value: start.get('to').getTime()});
            }
        }

        if (this.finishedBetween) {
            var end = this.getFinishedBetween();
            if (end.get('from')) {
                data.push({property: 'finishIntervalFrom', value: end.get('from').getTime()});
            }

            if (end.get('to')) {
                data.push({property: 'finishIntervalTo', value: end.get('to').getTime()});
            }
        }

        return data;
    }
});