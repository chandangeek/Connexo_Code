Ext.define('Mdc.model.CommandLimitRule',{
    extend: 'Uni.model.Version',

    requires: [
        'Uni.model.PendingChange'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name:'name', type: 'string', useNull: true},
        {name:'active', type: 'boolean'},
        {name:'dayLimit', type: 'int'},
        {name:'weekLimit', type: 'int'},
        {name:'monthLimit', type: 'int'},
        {name:'commands', type: 'auto'},
        {name:'statusMessage', type: 'string', useNull: true},

        {
            name: 'statusWithMessage',
            persist: false,
            mapping: function (data) {
                var pendingChanges = data.statusMessage,
                    icon = Ext.isEmpty(pendingChanges) ? '' :
                        '<span class="icon-info" style="margin-left:10px; position:absolute;" data-qtip="' + pendingChanges + '"></span>',
                    text = data.active ? Uni.I18n.translate('general.active', 'MDC', 'Active') : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                return text + icon;
            }
        }
    ],

    associations: [
        {
            name: 'changes',
            type: 'hasMany',
            model: 'Uni.model.PendingChange',
            associationKey: 'changes',
            foreignKey: 'changes',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.PendingChange';
            }
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/crr/commandrules',
        reader: {
            type: 'json'
        }
    }
});
