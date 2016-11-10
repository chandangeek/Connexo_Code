Ext.define('Imt.usagepointgroups.store.UsagePointsOfUsagePointGroupWithoutPagination', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.usagepointmanagement.model.UsagePoint'
    ],
    model: 'Imt.usagepointmanagement.model.UsagePoint',
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/usagepointgroups/{usagePointGroupId}/usagepoints',
        reader: {
            type: 'json',
            root: 'usagepoints'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        setUrl: function (usagePointGroupId) {
            this.url = this.urlTpl.replace('{usagePointGroupId}', usagePointGroupId);
        }
    }
});