/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ValidationRuleSetVersionRule', {
    extend: 'Ext.data.Model',	
    fields: [      
		{
			name: 'name'
		}
    ],	
   
	proxy: {
        type: 'rest',
        urlTpl: '/api/val/validation/{ruleSetId}/versions/{versionId}/rules/{ruleId}',
        reader: {
            type: 'json'
        },

        setUrl: function (ruleSetId, versionId, ruleId) {
            this.url = this.urlTpl.replace('{ruleSetId}', ruleSetId).replace('{versionId}', versionId).replace('{ruleId}', ruleId);
        }
    }
});
 