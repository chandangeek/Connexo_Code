/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.CustomEventTypeReference', {
    extend: 'Uni.property.view.property.Reference',

	comTaskEventTypes: ['CONNECTION_LOST', 'DEVICE_COMMUNICATION_FAILURE', 'UNABLE_TO_CONNECT'],
	
	customHandlerLogic: function() {
		
		var me = this,
			comTaskFilter = me.up().getComponent('BasicDataCollectionRuleTemplate.excludedComTasks'),
			currentValue = me.getValue();
		if(comTaskFilter) {
			if(currentValue) {
				if (me.comTaskEventTypes.includes(me.getValue())) {
					comTaskFilter.enableComponent();
				} else {
					comTaskFilter.disableComponent();
				}
			} else {
				comTaskFilter.disableComponent();
			}
		}
    }	
});