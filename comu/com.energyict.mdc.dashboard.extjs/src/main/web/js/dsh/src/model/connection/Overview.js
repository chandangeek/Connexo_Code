Ext.define('Dsh.model.connection.Overview', {
    extend: 'Dsh.model.Filterable',
    requires: [
        'Dsh.model.Summary',
        'Dsh.model.Counter',
        'Dsh.model.Breakdown',
        'Dsh.model.Kpi'
    ],
    hasOne: {
        model: 'Dsh.model.Summary',
        associationKey: 'connectionSummary',
        name: 'summary',
        getterName: 'getSummary',
        setterName: 'setSummary'
    },
    hasMany: [
        {
            model: 'Dsh.model.Counter',
            name: 'overviews'
        },
        {
            model: 'Dsh.model.Breakdown',
            name: 'breakdowns'
        },
        {
            model: 'Dsh.model.Kpi',
            name: 'kpi'
        }
    ],
    proxy: {
        type: 'ajax',
        url: '/api/dsr/connectionoverview'
    }
});