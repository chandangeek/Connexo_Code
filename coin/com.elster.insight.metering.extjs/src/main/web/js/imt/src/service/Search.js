Ext.define('Imt.service.Search', {
    extend: 'Uni.service.Search',

    init: function() {
        var me = this;
        me.defaultColumns = {
            'com.elster.jupiter.metering.EndDevice': ['mRID', 'serialNumber', 'name'],
            'com.elster.jupiter.metering.UsagePoint' : ['mRID', 'displayServiceCategory', 'displayConnectionState', 'name', 'location']
        };
        me.callParent(arguments);
    },
    createColumnDefinitionFromModel: function (field) {
        var me = this,
            column = this.callParent(arguments);

        if (column && column.dataIndex === 'mRID') {
            if (me.searchDomain.getId() === 'com.elster.jupiter.metering.UsagePoint') {
                column.renderer = function (value, metaData, record) {
                    var url = me.router.getRoute('usagepoints/view').buildUrl({mRID: encodeURIComponent(value)});
                    metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
            } else if (me.searchDomain.getId() === 'com.elster.jupiter.metering.EndDevice') {
                column.renderer = function(value) {
                    var url = me.router.getRoute('usagepoints/device').buildUrl({deviceMRID: value});
                    metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                }
            }
        }
        else {
            column.renderer = function (value, metaData, record) {
                // stupid solution to resolve encoding in tooltip
                metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                return Ext.String.htmlEncode(value);
            }
        }

        return column;
    }
});
