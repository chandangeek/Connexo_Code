/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetVersionPeriod', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'start', dateFormat: 'time', type: 'date'},
        {name: 'end', dateFormat: 'time', type: 'date'}
    ],

    proxy: {
        type: 'rest',

        setUsagePointUrl: function (usagePointId, customPropertySetId) {
            var url = '/api/udr/usagepoints/{usagePointId}/customproperties/{customPropertySetId}';

            this.url = url.replace('{usagePointId}', encodeURIComponent(usagePointId)).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});