Ext.define('Mdc.model.CommandLimitRule',{
    extend: 'Uni.model.Version',

    requires: [
        'Mdc.model.DualControlInfo',
        'Mdc.model.Command'
    ],

    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name:'name', type: 'string', useNull: true},
        {name:'active', type: 'boolean'},
        {name:'dayLimit', type: 'int'},
        {name:'weekLimit', type: 'int'},
        {name:'monthLimit', type: 'int'},
        {name:'commands', persist: false},
        {name:'statusMessage', type: 'string', useNull: true},
        {name: 'availableActions', type: 'auto', persist:false},

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
            name: 'dualControl',
            type: 'hasOne',
            model: 'Mdc.model.DualControlInfo',
            associationKey: 'dualControl',
            getterName: 'getDualControl',
            setterName: 'setDualControl'
        },
        {
            name: 'commands',
            type: 'hasMany',
            model: 'Mdc.model.Command',
            associationKey: 'commands',
            foreignKey: 'commands',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.Command';
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
