Ext.define('Imt.service.Search', {
    extend: 'Uni.service.Search',

    init: function() {
        var me = this;
        me.defaultColumns = {
            'com.elster.jupiter.metering.EndDevice': ['name', 'serialNumber'],
            'com.elster.jupiter.metering.UsagePoint': ['name', 'displayServiceCategory', 'displayMetrologyConfiguration', 'displayType', 'state', 'displayConnectionState', 'location']
        };
        me.callParent(arguments);
    },
    createColumnDefinitionFromModel: function (field) {
        var me = this,
            column = this.callParent(arguments);

        if (column && column.dataIndex === 'name') {
            if (me.searchDomain.getId() === 'com.elster.jupiter.metering.UsagePoint') {
                column.renderer = function (value, metaData) {
                    var url = me.router.getRoute('usagepoints/view').buildUrl({usagePointId: encodeURIComponent(value)});
                    metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                }
            } else if (me.searchDomain.getId() === 'com.elster.jupiter.metering.EndDevice') {
                column.renderer = function(value, metaData) {
                    var url = me.router.getRoute('usagepoints/device').buildUrl({deviceId: value});
                    metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                }
            }
        }
        else if (column.xtype != 'uni-date-column'
            && column.xtype != 'uni-grid-column-search-boolean'
            && column.xtype != 'uni-grid-column-search-devicetype'
            && column.xtype != 'uni-grid-column-search-deviceconfiguration'
            && column.xtype != 'uni-grid-column-search-quantity') {
            column.renderer = function (value, metaData, record) {
                // stupid solution to resolve encoding in tooltip
                metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                return Ext.String.htmlEncode(value) || '-';
            }
        }

        return column;
    }
});
