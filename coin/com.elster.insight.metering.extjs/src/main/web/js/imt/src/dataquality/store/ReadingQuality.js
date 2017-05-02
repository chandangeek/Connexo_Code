/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dataquality.store.ReadingQuality', {
     extend: 'Ext.data.Store',
        fields: [
            {name: 'id', type: 'string'},
            {name: 'name', type: 'string'}
        ],

        data: [
            {
                id: 'suspects',
                name: Uni.I18n.translate('general.suspects', 'IMT', 'Suspects')
            },
            {
                id: 'confirmed',
                name: Uni.I18n.translate('general.confirmed', 'IMT', 'Confirmed')
            },
            {
                id: 'estimates',
                name: Uni.I18n.translate('general.estimates', 'IMT', 'Estimates')
            },
            {
                id: 'informatives',
                name: Uni.I18n.translate('general.informatives', 'IMT', 'Informatives')
            },
            {
                id: 'edited',
                name: Uni.I18n.translate('general.edited', 'IMT', 'Edited')
            },
            {
                id: 'projected',
                name: Uni.I18n.translate('general.projected', 'IMT', 'Projected')
            }
        ]
});