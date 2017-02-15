/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.startprocess.model.ProcessContent', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        'status', 'deploymentId', 'businessObject', 'versionDB', 'processVersion', 'processName'
    ],
    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/bpm/runtime/processcontent/{deploymentId}',
        reader: {
            type: 'json'
        },
        setUrl: function (deploymentId) {
            this.url = this.urlTpl.replace('{deploymentId}', encodeURIComponent(deploymentId));
        }
    }
});