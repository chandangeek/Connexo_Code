/**
 * @class Uni.model.AppItem
 */
Ext.define('Uni.model.AppItem', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'basePath',
        'startPage',
        'icon',
        'mainController',
        'scripts',
        'translationComponents',
        'styleSheets',
        'dependencies'
    ]
});