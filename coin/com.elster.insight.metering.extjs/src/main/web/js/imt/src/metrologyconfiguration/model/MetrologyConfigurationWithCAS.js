Ext.define('Imt.metrologyconfiguration.model.MetrologyConfigurationWithCAS', {
    extend: 'Imt.metrologyconfiguration.model.MetrologyConfiguration',

    associations: [
        {
            name: 'customPropertySets',
            type: 'hasMany',
            model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',
            associationKey: 'customPropertySets',
            foreignKey: 'customPropertySets'
        }
    ]
});