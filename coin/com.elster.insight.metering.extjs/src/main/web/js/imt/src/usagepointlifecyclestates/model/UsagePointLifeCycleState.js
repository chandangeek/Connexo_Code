Ext.define('Imt.usagepointlifecyclestates.model.UsagePointLifeCycleState', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Imt.usagepointlifecyclestates.model.TransitionBusinessProcess'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isCustom', type: 'boolean'},
        {name: 'isInitial', type: 'boolean'},
        {
            name: 'display_name',
            persist: false,
            mapping: function (data) {
                return data.name;
            }
        },
        {name: 'onEntry', type: 'auto', defaultValue: []},
        {name: 'onExit', type: 'auto', defaultValue: []}
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
    }
});
