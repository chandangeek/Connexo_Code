/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.model.connection.Overview', {
    extend: 'Dsh.model.Filterable',
    requires: [
        'Dsh.model.Summary',
        'Dsh.model.Counter',
        'Dsh.model.Breakdown',
        'Dsh.model.Kpi'
    ],
    hasOne: [
        {
            model: 'Dsh.model.Summary',
            associationKey: 'connectionSummary',
            name: 'summary',
            getterName: 'getSummary',
            setterName: 'setSummary'
        },
        {
            model: 'Dsh.model.Kpi',
            associationKey: 'kpi',
            name: 'kpi',
            getterName: 'getKpi',
            setterName: 'setKpi'
        }
    ],
    hasMany: [
        {
            model: 'Dsh.model.Counter',
            name: 'overviews'
        },
        {
            model: 'Dsh.model.Breakdown',
            name: 'breakdowns'
        }
    ],
    proxy: {
        type: 'ajax',
        url: '/api/dsr/connectionoverview'
    }
});