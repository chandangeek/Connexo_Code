/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.Periods
 */
Ext.define('Uni.store.Periods', {
    extend: 'Ext.data.ArrayStore',

    data: [
        ['years',
            Uni.I18n.translate('period.years', 'UNI', 'year(s)'),
            function(n) {return Uni.I18n.translatePlural('general.timeUnit.years', n, 'UNI', '{0} years', '{0} year', '{0} years');}
        ],
        ['months',
            Uni.I18n.translate('period.months', 'UNI', 'month(s)'),
            function(n) {return Uni.I18n.translatePlural('general.timeUnit.months', n, 'UNI', '{0} months', '{0} month', '{0} months');}
        ],
        ['weeks',
            Uni.I18n.translate('period.weeks', 'UNI', 'week(s)'),
            function(n) {return Uni.I18n.translatePlural('general.timeUnit.weeks', n, 'UNI', '{0} weeks', '{0} week', '{0} weeks');}
        ],
        ['days',
            Uni.I18n.translate('period.days', 'UNI', 'day(s)'),
            function(n) {return Uni.I18n.translatePlural('general.timeUnit.days', n, 'UNI', '{0} days', '{0} day', '{0} days');}
        ],
        ['hours',
            Uni.I18n.translate('period.hours', 'UNI', 'hour(s)'),
            function(n) {return Uni.I18n.translatePlural('general.timeUnit.hours', n, 'UNI', '{0} hours', '{0} hour', '{0} hours');}
        ],
        ['minutes',
            Uni.I18n.translate('period.minutes', 'UNI', 'minute(s)'),
            function(n) {return Uni.I18n.translatePlural('general.timeUnit.minutes', n, 'UNI', '{0} minutes', '{0} minute', '{0} minutes');}
        ]
    ],

    fields: ['value', 'name', 'translate']

});