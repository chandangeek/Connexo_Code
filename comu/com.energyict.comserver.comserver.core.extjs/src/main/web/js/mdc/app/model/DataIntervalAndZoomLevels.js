Ext.define('Mdc.model.DataIntervalAndZoomLevels', {
    extend: 'Ext.data.Model',
    fields: [
        'interval',
        'all',
        'intervalInMs',
        'zoomLevels'
    ],
    idProperty: 'interval'
});