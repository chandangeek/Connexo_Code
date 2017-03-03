/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.EstimationRulesOnChannelMainValue', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.EstimationRuleOnChannel',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/data/estimateWithRule',
        reader: {
            type: 'json',
            root: 'rules',
            totalProperty: 'total'
        }
    }
});