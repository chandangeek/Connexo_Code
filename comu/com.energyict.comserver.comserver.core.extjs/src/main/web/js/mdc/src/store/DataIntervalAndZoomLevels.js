Ext.define('Mdc.store.DataIntervalAndZoomLevels', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.DataIntervalAndZoomLevels',


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
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', '{0} days', '{0} day', '{0} days')
                },
                {
                    id: '12hours',
                    count: 12,
                    timeUnit: 'hours',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.hours', 12, 'MDC', '{0} hours', '{0} hour', '{0} hours')
                },
                {
                    id: '4hours',
                    count: 4,
                    timeUnit: 'hours',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.hours', 4, 'MDC', '{0} hours', '{0} hour', '{0} hours')
                },
                {
                    id: '1hours',
                    count: 1,
                    timeUnit: 'hours',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.hours', 1, 'MDC', '{0} hours', '{0} hour', '{0} hours')
                },
                {
                    id: '15minutes',
                    count: 15,
                    timeUnit: 'minutes',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.minutes', 15, 'MDC', '{0} minutes', '{0} minute', '{0} minutes')
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
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', '{0} weeks', '{0} week', '{0} weeks')
                },
                {
                    id: '2days',
                    count: 2,
                    timeUnit: 'days',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.days', 2, 'MDC', '{0} days', '{0} day', '{0} days')
                },
                {
                    id: '1days',
                    count: 1,
                    timeUnit: 'days',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', '{0} days', '{0} day', '{0} days')
                },
                {
                    id: '4hours',
                    count: 4,
                    timeUnit: 'hours',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.hours', 4, 'MDC', '{0} hours', '{0} hour', '{0} hours')
                },
                {
                    id: '1hours',
                    count: 1,
                    timeUnit: 'hours',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.hours', 1, 'MDC', '{0} hours', '{0} hour', '{0} hours')
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
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', '{0} months', '{0} month', '{0} months')
                },
                {
                    id: '2weeks',
                    count: 2,
                    timeUnit: 'weeks',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 2, 'MDC', '{0} weeks', '{0} week', '{0} weeks')
                },
                {
                    id: '1weeks',
                    count: 1,
                    timeUnit: 'weeks',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', '{0} weeks', '{0} week', '{0} weeks')
                },
                {
                    id: '2days',
                    count: 2,
                    timeUnit: 'days',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.days', 2, 'MDC', '{0} days', '{0} day', '{0} days')
                },
                {
                    id: '1days',
                    count: 1,
                    timeUnit: 'days',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', '{0} days', '{0} day', '{0} days')
                },
                {
                    id: '4hours',
                    count: 4,
                    timeUnit: 'hours',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.hours', 4, 'MDC', '{0} hours', '{0} hour', '{0} hours')
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
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 2, 'MDC', '{0} months', '{0} month', '{0} months')
                },
                {
                    id: '1months',
                    count: 1,
                    timeUnit: 'months',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', '{0} months', '{0} month', '{0} months')
                },
                {
                    id: '2weeks',
                    count: 2,
                    timeUnit: 'weeks',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 2, 'MDC', '{0} weeks', '{0} week', '{0} weeks')
                },
                {
                    id: '1weeks',
                    count: 1,
                    timeUnit: 'weeks',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', '{0} weeks', '{0} week', '{0} weeks')
                },
                {
                    id: '1days',
                    count: 1,
                    timeUnit: 'days',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', '{0} days', '{0} day', '{0} days')
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
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '6months',
                    count: 6,
                    timeUnit: 'months',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 6, 'MDC', '{0} months', '{0} month', '{0} months')
                },
                {
                    id: '3months',
                    count: 3,
                    timeUnit: 'months',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 3, 'MDC', '{0} months', '{0} month', '{0} months')
                },
                {
                    id: '1months',
                    count: 1,
                    timeUnit: 'months',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', '{0} months', '{0} month', '{0} months')
                },
                {
                    id: '1weeks',
                    count: 1,
                    timeUnit: 'weeks',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', '{0} weeks', '{0} week', '{0} weeks')
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
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 2, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '1years',
                    count: 1,
                    timeUnit: 'years',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '6months',
                    count: 6,
                    timeUnit: 'months',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 6, 'MDC', '{0} months', '{0} month', '{0} months')
                },
                {
                    id: '3months',
                    count: 3,
                    timeUnit: 'months',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 3, 'MDC', '{0} months', '{0} month', '{0} months')
                },
                {
                    id: '1months',
                    count: 1,
                    timeUnit: 'months',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', '{0} months', '{0} month', '{0} months')
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
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 10, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '5years',
                    count: 5,
                    timeUnit: 'years',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 5, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '1years',
                    count: 1,
                    timeUnit: 'years',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '6months',
                    count: 6,
                    timeUnit: 'months',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 6, 'MDC', '{0} months', '{0} month', '{0} months')
                },
                {
                    id: '3months',
                    count: 3,
                    timeUnit: 'months',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 3, 'MDC', '{0} months', '{0} month', '{0} months')
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
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 20, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '10years',
                    count: 10,
                    timeUnit: 'years',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 10, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '5years',
                    count: 5,
                    timeUnit: 'years',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 5, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '1years',
                    count: 1,
                    timeUnit: 'years',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', '{0} years', '{0} year', '{0} years')
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
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 20, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '10years',
                    count: 10,
                    timeUnit: 'years',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 10, 'MDC', '{0} years', '{0} year', '{0} years')
                },
                {
                    id: '5years',
                    count: 5,
                    timeUnit: 'years',
                    localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 5, 'MDC', '{0} years', '{0} year', '{0} years')
                }
            ]
        }
    ]
});