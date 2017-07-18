/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by david on 6/6/2016.
 */
Ext.define('Uni.store.DaysOfWeek', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'key',
            type: 'string'
        },
        {
            name: 'translation',
            type: 'string'
        }
    ],
    data: [
        {
            id: 1,
            key: 'monday',
            translation: Uni.I18n.translate('schedulefield.monday', 'UNI', 'Monday')
        },
        {
            id: 2,
            key: 'tuesday',
            translation: Uni.I18n.translate('schedulefield.tuesday', 'UNI', 'Tuesday')
        },
        {
            id: 3,
            key: 'wednesday',
            translation: Uni.I18n.translate('schedulefield.wednesday', 'UNI', 'Wednesday')
        },
        {
            id: 4,
            key: 'thursday',
            translation: Uni.I18n.translate('schedulefield.thursday', 'UNI', 'Thursday')
        },
        {
            id: 5,
            key: 'friday',
            translation: Uni.I18n.translate('schedulefield.friday', 'UNI', 'Friday')
        },
        {
            id: 6,
            key: 'saturday',
            translation: Uni.I18n.translate('schedulefield.saturday', 'UNI', 'Saturday')
        },
        {
            id: 7,
            key: 'sunday',
            translation: Uni.I18n.translate('schedulefield.sunday', 'UNI', 'Sunday')
        }
    ]
});