Ext.define('Mdc.model.Command',{
    extend: 'Ext.data.Model',
    fields: [
        {name:'commandName', type: 'string', useNull: true},
        {name:'command', type: 'string', useNull: true},
        {name:'category', type: 'string', useNull: true},
        {
            name: 'displayName',
            persist: false,
            mapping: function(data) {
                return (data.command && data.category)
                    ? data.category + ' - ' + data.command
                    : '?';
            }
        }
    ]
});
