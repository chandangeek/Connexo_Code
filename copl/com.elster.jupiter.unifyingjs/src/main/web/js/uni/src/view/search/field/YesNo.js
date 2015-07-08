/**
* @class Uni.view.search.field.YesNo
*/
Ext.define('Uni.view.search.field.YesNo', {
    extend: 'Ext.form.field.ComboBox',
    xtype: 'uni-view-search-field-yesno',

    emptyText: Uni.I18n.translate('view.search.field.yesno.label', 'FWC', 'Text'),
    displayField: 'name',
    valueField: 'value',
    width: 190,
    store: Ext.create('Ext.data.Store', {
        fields: ['name', 'value'],
        data: [
            {'name': Uni.I18n.translate('window.messabox.yes', 'FWC', 'Yes'), 'value': 'true'},
            {'name': Uni.I18n.translate('window.messabox.no', 'FWC', 'No'), 'value': 'false'}
        ]
}),
    queryMode: 'local',
    forceSelection: true,
    multiSelect: false,

    initComponent: function () {
        this.callParent(arguments);
    }

});