Ext.define('Mdc.model.AuthenticationLevel', {
    extend: 'Ext.data.Model',
    statics: {
        noAuthentication: function()  {
            var level = new this();
            level.set('id', -1);
            level.set('name',Uni.I18n.translate('AuthenticationLevel.noAuthentication', 'MDC', 'No Authentication'));
            return level;
        }
    },
    fields: [
        {name: 'id', type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true}
    ]

});