/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions', {
    extend: 'Ext.data.Store',
    model: 'Imt.customattributesonvaluesobjects.model.ConflictedAttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'conflicts'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUsagePointUrl: function (usagePointId, customPropertySetId) {
            var url = '/api/udr/usagepoints/{usagePointId}/customproperties/{customPropertySetId}/conflicts';

            this.url = url
                .replace('{usagePointId}', encodeURIComponent(usagePointId))
                .replace('{customPropertySetId}', customPropertySetId);
        },

        setUsagePointEditUrl: function (usagePointId, customPropertySetId, versionId) {
            var url = '/api/udr/usagepoints/{usagePointId}/customproperties/{customPropertySetId}/conflicts/{versionId}';

            this.url = url
                .replace('{usagePointId}', encodeURIComponent(usagePointId))
                .replace('{customPropertySetId}', customPropertySetId)
                .replace('{versionId}', versionId);
        }
    }
});