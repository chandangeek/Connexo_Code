/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.store.ReadingQualities
 */
Ext.define('Uni.store.ReadingQualities', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'id', type: 'string'},
        {name: 'name', type: 'string'}
    ],

    data: [
        {
            id: 'suspects',
            name: Uni.I18n.translate('general.suspects', 'UNI', 'Suspects')
        },
        {
            id: 'confirmed',
            name: Uni.I18n.translate('general.confirmed', 'UNI', 'Confirmed')
        },
        {
            id: 'estimates',
            name: Uni.I18n.translate('general.estimates', 'UNI', 'Estimates')
        },
        {
            id: 'informatives',
            name: Uni.I18n.translate('general.informatives', 'UNI', 'Informatives')
        },
        {
            id: 'edited',
            name: Uni.I18n.translate('general.edited', 'UNI', 'Edited')
        }
    ]
});
