Ext.define('Mdc.model.EncryptionLevel', {
    extend: 'Ext.data.Model',
    statics: {
        noEncryption: function()  {
            var level = new this();
            level.set('id', -1);
            level.set('name', Uni.I18n.translate('EncryptionLevel.noEncryption', 'MDC', 'No encryption'));
            return level;
        }
    },
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true}
    ]
});