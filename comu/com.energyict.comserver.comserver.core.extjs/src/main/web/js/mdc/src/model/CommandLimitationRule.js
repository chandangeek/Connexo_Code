Ext.define('Mdc.model.CommandLimitationRule',{
    extend: 'Uni.model.Version',
    requires: [
    ],
    fields: [
        {name:'id', type: 'int', useNull: true},
        {name:'name', type: 'string'},
        {name:'active', type:'boolean', defaultValue:false},
        {name:'dayLimit', type: 'int', defaultValue:0},
        {name:'weekLimit', type: 'int', defaultValue:0},
        {name:'monthLimit', type: 'int', defaultValue:0},
        {name:'statusMessage', type:'string', persist:false},
        {name:'commands', type:'auto'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/crr/commandrules',
        reader: {
            type: 'json'
        }
    }
});