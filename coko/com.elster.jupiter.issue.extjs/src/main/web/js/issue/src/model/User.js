Ext.define('Isu.model.User', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    fields: [
        {
            name: 'id',
            displayValue: Uni.I18n.translate('general.id','ISU','ID'),
            type: 'int'
        },
        {
            name: 'type',
            displayValue: Uni.I18n.translate('general.type','ISU','Type'),
            type: 'auto'
        },
        {
            name: 'name',
            displayValue: Uni.I18n.translate('general.name','ISU','Name'),
            type: 'auto'
        }
    ],

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees/users',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});