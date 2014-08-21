Ext.define('Mdc.store.DataIntervalAndZoomLevels', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.DataIntervalAndZoomLevels',

    getIntervalStart: function (count, timeUnit, intervalEnd) {
        var intervalStart,
            intervalEndDate = new Date(intervalEnd);

        switch (timeUnit) {
            case 'day':
                intervalStart = intervalEndDate.getTime() - count * 86400000;
                break;
            case 'week':
                intervalStart = intervalEndDate.getTime() - count * 604800000;
                break;
            case 'month':
                intervalStart = intervalEndDate.setMonth(intervalEndDate.getMonth() - count);
                break;
            case 'year':
                intervalStart = intervalEndDate.setFullYear(intervalEndDate.getFullYear() - count);
                break;
        }
        return intervalStart;
    },

    data: [
        {
            interval: '1minutes',
            all: {
                count: 1,
                timeUnit: 'day'
            },
            intervalInMs: 60000,
            zoomLevels: [
                {
                    type: 'minute',
                    count: 15,
                    text: '15m'
                },
                {
                    type: 'hour',
                    count: 1,
                    text: '1h'
                },
                {
                    type: 'hour',
                    count: 4,
                    text: '4h'
                },
                {
                    type: 'hour',
                    count: 12,
                    text: '12h'
                }
            ]
        },
        {
            interval: '5minutes',
            all: {
                count: 1,
                timeUnit: 'week'
            },
            intervalInMs: 300000,
            zoomLevels: [
                {
                    type: 'hour',
                    count: 1,
                    text: '1h'
                },
                {
                    type: 'hour',
                    count: 4,
                    text: '4h'
                },
                {
                    type: 'day',
                    count: 1,
                    text: '1d'
                },
                {
                    type: 'day',
                    count: 2,
                    text: '2d'
                }
            ]
        },
        {
            interval: '15minutes',
            all: {
                count: 2,
                timeUnit: 'week'
            },
            intervalInMs: 900000,
            zoomLevels: [
                {
                    type: 'hour',
                    count: 4,
                    text: '4h'
                },
                {
                    type: 'day',
                    count: 1,
                    text: '1d'
                },
                {
                    type: 'day',
                    count: 2,
                    text: '2d'
                },
                {
                    type: 'week',
                    count: 1,
                    text: '1w'
                }
            ]
        },
        {
            interval: '1hours',
            all: {
                count: 2,
                timeUnit: 'month'
            },
            intervalInMs: 3600000,
            zoomLevels: [
                {
                    type: 'day',
                    count: 1,
                    text: '1d'
                },
                {
                    type: 'week',
                    count: 1,
                    text: '1w'
                },
                {
                    type: 'week',
                    count: 2,
                    text: '2w'
                },
                {
                    type: 'month',
                    count: 1,
                    text: '1m'
                }
            ]
        },
        {
            interval: '1days',
            all: {
                count: 1,
                timeUnit: 'year'
            },
            intervalInMs: 86400000,
            zoomLevels: [
                {
                    type: 'week',
                    count: 1,
                    text: '1w'
                },
                {
                    type: 'month',
                    count: 1,
                    text: '1m'
                },
                {
                    type: 'month',
                    count: 3,
                    text: '3m'
                },
                {
                    type: 'month',
                    count: 6,
                    text: '6m'
                }
            ]
        },
        {
            interval: '1weeks',
            all: {
                count: 2,
                timeUnit: 'year'
            },
            intervalInMs: 604800000,
            zoomLevels: [
                {
                    type: 'month',
                    count: 1,
                    text: '1m'
                },
                {
                    type: 'month',
                    count: 3,
                    text: '3m'
                },
                {
                    type: 'month',
                    count: 6,
                    text: '6m'
                },
                {
                    type: 'year',
                    count: 1,
                    text: '1y'
                }
            ]
        },
        {
            interval: '1months',
            all: {
                count: 10,
                timeUnit: 'year'
            },
            intervalInMs: 2678400000,
            zoomLevels: [
                {
                    type: 'month',
                    count: 3,
                    text: '3m'
                },
                {
                    type: 'month',
                    count: 6,
                    text: '6m'
                },
                {
                    type: 'year',
                    count: 1,
                    text: '1y'
                },
                {
                    type: 'year',
                    count: 5,
                    text: '5y'
                }
            ]
        },
        {
            interval: '3months',
            all:  {
                count: 20,
                timeUnit: 'year'
            },
            intervalInMs: 8035200000,
            zoomLevels: [
                {
                    type: 'year',
                    count: 1,
                    text: '1y'
                },
                {
                    type: 'year',
                    count: 5,
                    text: '5y'
                },
                {
                    type: 'year',
                    count: 10,
                    text: '10y'
                }
            ]
        },
        {
            interval: '1years',
            all:  {
                count: 20,
                timeUnit: 'year'
            },
            intervalInMs: 31536000000,
            zoomLevels: [
                {
                    type: 'year',
                    count: 5,
                    text: '5y'
                },
                {
                    type: 'year',
                    count: 10,
                    text: '10y'
                }
            ]
        }
    ]
});