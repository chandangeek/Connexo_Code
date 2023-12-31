/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecyclestates.model.UsagePointLifeCycleState', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Imt.usagepointlifecyclestates.model.TransitionBusinessProcess'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isInitial', type: 'boolean'},
        'stage',
        {name: 'onEntry', type: 'auto', defaultValue: [], persist: true},
        {name: 'onExit', type: 'auto', defaultValue: [], persist: true}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/upl/lifecycle/{id}/states/',
        reader: {
            type: 'json'
        },
        setUrl: function (params) {
            this.url = this.urlTpl.replace('{id}', params.usagePointLifeCycleId);
        }
    },

    setAsInitial: function (usagePointLifeCycleId, options) {
        var me = this;
        Ext.Ajax.request(Ext.Object.merge(
            {
                url: '/api/upl/lifecycle/' + usagePointLifeCycleId + '/states/' + me.get('id') + '/status',
                method: 'PUT',
                jsonData: me.getRecordData()
            }, options));
    }
});
