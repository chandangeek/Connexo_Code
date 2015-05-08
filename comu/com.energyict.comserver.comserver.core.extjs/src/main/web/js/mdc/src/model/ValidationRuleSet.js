Ext.define('Mdc.model.ValidationRuleSet', {
    extend: 'Ext.data.Model',	
    fields: [      
		{
			name: 'name'
		}
    ],	
   
	proxy: {
        type: 'rest',
        urlTpl: '/api/val/validation/{ruleSetId}',
        reader: {
            type: 'json'
        },

        setUrl: function (ruleSetId) {
            this.url = this.urlTpl.replace('{ruleSetId}', ruleSetId);
        }
    }
}); 
