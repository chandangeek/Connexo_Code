Ext.define('Imt.metrologyconfiguration.model.MetrologyContract', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.metrologyconfiguration.model.ReadingTypeDeliverable'
    ],
    fields: ['id', 'name','mandatory'],

    associations: [
        {
            name: 'readingTypeDeliverables',
            type: 'hasMany',
            model: 'Imt.metrologyconfiguration.model.ReadingTypeDeliverable',
            associationKey: 'readingTypeDeliverables'
        }
    ]
});