Ext.define('Imt.service.Search', {
    extend: 'Uni.service.Search',

    init: function() {
        var me = this;
        me.defaultColumns = {
            'com.elster.jupiter.metering.EndDevice': ['mRID', 'serialNumber', 'name'],
            'com.elster.jupiter.metering.UsagePoint' : ['mRID', 'serviceCategory', 'connectionState', 'name']
        };
        me.callParent(arguments);
    },
    createColumnDefinitionFromModel: function (field) {
        var me = this,
            column = this.callParent(arguments);

        if (column && column.dataIndex === 'mRID') {
            if (me.searchDomain.getId() === 'com.elster.jupiter.metering.UsagePoint') {
                column.renderer = function (value) {
                    var url = me.router.getRoute('usagepoints/view').buildUrl({mRID: value});
                    return '<a href="{0}">{1}</a>'.replace('{0}', url).replace('{1}', Ext.String.htmlEncode(value));
                }
            } else if (me.searchDomain.getId() === 'com.elster.jupiter.metering.EndDevice') {
                column.renderer = function(value) {
                    var url = me.router.getRoute('usagepoints/device').buildUrl({mRID: value});
                    return '<a href="{0}">{1}</a>'.replace('{0}', url).replace('{1}', Ext.String.htmlEncode(value));
                }
            }
        }

        return column;
    }
});
