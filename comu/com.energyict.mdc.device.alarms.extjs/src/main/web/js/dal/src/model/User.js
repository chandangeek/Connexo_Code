Ext.define('Dal.model.User', {
    extend: 'Ext.data.Model',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    fields: [
        {
            name: 'id',
            displayValue: Uni.I18n.translate('general.id','DAL','ID'),
            type: 'int'
        },
        {
            name: 'type',
            displayValue: Uni.I18n.translate('general.type','DAL','Type'),
            type: 'auto'
        },
        {
            name: 'name',
            displayValue: Uni.I18n.translate('general.name','DAL','Name'),
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