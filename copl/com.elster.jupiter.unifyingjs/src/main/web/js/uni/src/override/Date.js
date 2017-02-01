/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.Date', {
    override: 'Ext.Date',

    dayNames: [
        Uni.I18n.translate('general.day.sunday', 'UNI', 'Sunday'),
        Uni.I18n.translate('general.day.monday', 'UNI', 'Monday'),
        Uni.I18n.translate('general.day.tuesday', 'UNI', 'Tuesday'),
        Uni.I18n.translate('general.day.wednesday', 'UNI', 'Wednesday'),
        Uni.I18n.translate('general.day.thursday', 'UNI', 'Thursday'),
        Uni.I18n.translate('general.day.friday', 'UNI', 'Friday'),
        Uni.I18n.translate('general.day.saturday', 'UNI', 'Saturday')
    ],

    monthNames: [
        Uni.I18n.translate('general.month.january', 'UNI', 'January'),
        Uni.I18n.translate('general.month.february', 'UNI', 'February'),
        Uni.I18n.translate('general.month.march', 'UNI', 'March'),
        Uni.I18n.translate('general.month.april', 'UNI', 'April'),
        Uni.I18n.translate('general.month.may', 'UNI', 'May'),
        Uni.I18n.translate('general.month.june', 'UNI', 'June'),
        Uni.I18n.translate('general.month.july', 'UNI', 'July'),
        Uni.I18n.translate('general.month.august', 'UNI', 'August'),
        Uni.I18n.translate('general.month.september', 'UNI', 'September'),
        Uni.I18n.translate('general.month.october', 'UNI', 'October'),
        Uni.I18n.translate('general.month.november', 'UNI', 'November'),
        Uni.I18n.translate('general.month.december', 'UNI', 'December')
    ]
});