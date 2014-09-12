Ext.define('Mdc.model.SecuritySetting', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'authenticationLevel', type: 'auto', useNull: true},
        {name: 'encryptionLevel', type: 'auto', useNull: true}
    ]
});