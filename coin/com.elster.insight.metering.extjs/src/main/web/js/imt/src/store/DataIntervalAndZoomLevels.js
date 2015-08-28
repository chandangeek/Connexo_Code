Ext.define('Imt.store.DataIntervalAndZoomLevels', {
    extend: 'Ext.data.Store',
    model: 'Imt.model.DataIntervalAndZoomLevels',


    getIntervalRecord: function(interval) {
        var intervalInMs = this.getIntervalInMs(interval),
            neededRecord;

        this.each(function(value){
            if (intervalInMs >= value.get('intervalInMs')){
                neededRecord = value;
            } else {
                return false;
            }
        });

        return neededRecord;
    },

    getIntervalInMs: function(interval) {
        var intervalInMs;

        switch (interval.timeUnit) {
            case 'minutes':
                intervalInMs = interval.count * 60000;
                break;
            case 'hours':
                intervalInMs = interval.count * 3600000;
                break;
            case 'days':
                intervalInMs = interval.count * 86400000;
                break;
            case 'weeks':
                intervalInMs = interval.count * 604800000;
                break;
            case 'months':
                intervalInMs = interval.count * 2678400000;
                break;
            case 'years':
                intervalInMs = interval.count * 31536000000;
                break;
        }

        return intervalInMs
    },

    data: [
        {
            interval: '1minutes',
            all: {
                count: 1,
                timeUnit: 'days'
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
            ],
            duration: [
                {
                    id: '1days',
                    count: 1,
                    timeUnit: 'days',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', 'day')
                },
                {
                    id: '12hours',
                    count: 12,
                    timeUnit: 'hours',
                    localizeValue: 12 + ' ' + Uni.I18n.translatePlural('general.timeUnit.hours', 12, 'MDC', 'hours')
                },
                {
                    id: '4hours',
                    count: 4,
                    timeUnit: 'hours',
                    localizeValue: 4 + ' ' + Uni.I18n.translatePlural('general.timeUnit.hours', 4, 'MDC', 'hours')
                },
                {
                    id: '1hours',
                    count: 1,
                    timeUnit: 'hours',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.hours', 1, 'MDC', 'hour')
                },
                {
                    id: '15minutes',
                    count: 15,
                    timeUnit: 'minutes',
                    localizeValue: 15 + ' ' + Uni.I18n.translatePlural('general.timeUnit.minutes', 15, 'MDC', 'minutes')
                }
            ]
        },
        {
            interval: '5minutes',
            all: {
                count: 1,
                timeUnit: 'weeks'
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
            ],
            duration: [
                {
                    id: '1weeks',
                    count: 1,
                    timeUnit: 'weeks',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', 'week')
                },
                {
                    id: '2days',
                    count: 2,
                    timeUnit: 'days',
                    localizeValue: 2 + ' ' + Uni.I18n.translatePlural('general.timeUnit.days', 2, 'MDC', 'days')
                },
                {
                    id: '1days',
                    count: 1,
                    timeUnit: 'days',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', 'day')
                },
                {
                    id: '4hours',
                    count: 4,
                    timeUnit: 'hours',
                    localizeValue: 4 + ' ' + Uni.I18n.translatePlural('general.timeUnit.hours', 4, 'MDC', 'hours')
                },
                {
                    id: '1hours',
                    count: 1,
                    timeUnit: 'hours',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.hours', 1, 'MDC', 'hour')
                }
            ]
        },
        {
            interval: '15minutes',
            all: {
                count: 1,
                timeUnit: 'months'
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
            ],
            duration: [
                {
                    id: '1months',
                    count: 1,
                    timeUnit: 'months',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', 'month')
                },
                {
                    id: '2weeks',
                    count: 2,
                    timeUnit: 'weeks',
                    localizeValue: 2 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 2, 'MDC', 'weeks')
                },
                {
                    id: '1weeks',
                    count: 1,
                    timeUnit: 'weeks',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', 'week')
                },
                {
                    id: '2days',
                    count: 2,
                    timeUnit: 'days',
                    localizeValue: 2 + ' ' + Uni.I18n.translatePlural('general.timeUnit.days', 2, 'MDC', 'days')
                },
                {
                    id: '1days',
                    count: 1,
                    timeUnit: 'days',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', 'day')
                },
                {
                    id: '4hours',
                    count: 4,
                    timeUnit: 'hours',
                    localizeValue: 4 + ' ' + Uni.I18n.translatePlural('general.timeUnit.hours', 4, 'MDC', 'hours')
                }
            ]
        },
        {
            interval: '1hours',
            all: {
                count: 2,
                timeUnit: 'months'
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
            ],
            duration: [
                {
                    id: '2months',
                    count: 2,
                    timeUnit: 'months',
                    localizeValue: 2 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 2, 'MDC', 'month')
                },
                {
                    id: '1months',
                    count: 1,
                    timeUnit: 'months',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', 'month')
                },
                {
                    id: '2weeks',
                    count: 2,
                    timeUnit: 'weeks',
                    localizeValue: 2 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 2, 'MDC', 'weeks')
                },
                {
                    id: '1weeks',
                    count: 1,
                    timeUnit: 'weeks',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', 'week')
                },
                {
                    id: '1days',
                    count: 1,
                    timeUnit: 'days',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', 'day')
                }
            ]
        },
        {
            interval: '1days',
            all: {
                count: 1,
                timeUnit: 'years'
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
            ],
            duration: [
                {
                    id: '1years',
                    count: 1,
                    timeUnit: 'years',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', 'year')
                },
                {
                    id: '6months',
                    count: 6,
                    timeUnit: 'months',
                    localizeValue: 6 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 6, 'MDC', 'months')
                },
                {
                    id: '3months',
                    count: 3,
                    timeUnit: 'months',
                    localizeValue: 3 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 3, 'MDC', 'months')
                },
                {
                    id: '1months',
                    count: 1,
                    timeUnit: 'months',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', 'month')
                },
                {
                    id: '1weeks',
                    count: 1,
                    timeUnit: 'weeks',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', 'week')
                }
            ]
        },
        {
            interval: '1weeks',
            all: {
                count: 2,
                timeUnit: 'years'
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
            ],
            duration: [
                {
                    id: '2years',
                    count: 2,
                    timeUnit: 'years',
                    localizeValue: 2 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 2, 'MDC', 'years')
                },
                {
                    id: '1years',
                    count: 1,
                    timeUnit: 'years',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', 'year')
                },
                {
                    id: '6months',
                    count: 6,
                    timeUnit: 'months',
                    localizeValue: 6 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 6, 'MDC', 'months')
                },
                {
                    id: '3months',
                    count: 3,
                    timeUnit: 'months',
                    localizeValue: 3 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 3, 'MDC', 'months')
                },
                {
                    id: '1months',
                    count: 1,
                    timeUnit: 'months',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', 'month')
                }
            ]
        },
        {
            interval: '1months',
            all: {
                count: 10,
                timeUnit: 'years'
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
            ],
            duration: [
                {
                    id: '10years',
                    count: 10,
                    timeUnit: 'years',
                    localizeValue: 10 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 10, 'MDC', 'years')
                },
                {
                    id: '5years',
                    count: 5,
                    timeUnit: 'years',
                    localizeValue: 5 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 5, 'MDC', 'years')
                },
                {
                    id: '1years',
                    count: 1,
                    timeUnit: 'years',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', 'year')
                },
                {
                    id: '6months',
                    count: 6,
                    timeUnit: 'months',
                    localizeValue: 6 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 6, 'MDC', 'months')
                },
                {
                    id: '3months',
                    count: 3,
                    timeUnit: 'months',
                    localizeValue: 3 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 3, 'MDC', 'months')
                }
            ]
        },
        {
            interval: '3months',
            all:  {
                count: 20,
                timeUnit: 'years'
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
            ],
            duration: [
                {
                    id: '20years',
                    count: 20,
                    timeUnit: 'years',
                    localizeValue: 20 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 20, 'MDC', 'years')
                },
                {
                    id: '10years',
                    count: 10,
                    timeUnit: 'years',
                    localizeValue: 10 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 10, 'MDC', 'years')
                },
                {
                    id: '5years',
                    count: 5,
                    timeUnit: 'years',
                    localizeValue: 5 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 5, 'MDC', 'years')
                },
                {
                    id: '1years',
                    count: 1,
                    timeUnit: 'years',
                    localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', 'year')
                }
            ]
        },
        {
            interval: '1years',
            all:  {
                count: 20,
                timeUnit: 'years'
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
            ],
            duration: [
                {
                    id: '20years',
                    count: 20,
                    timeUnit: 'years',
                    localizeValue: 20 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 20, 'MDC', 'years')
                },
                {
                    id: '10years',
                    count: 10,
                    timeUnit: 'years',
                    localizeValue: 10 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 10, 'MDC', 'years')
                },
                {
                    id: '5years',
                    count: 5,
                    timeUnit: 'years',
                    localizeValue: 5 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 5, 'MDC', 'years')
                }
            ]
        }
    ]
});