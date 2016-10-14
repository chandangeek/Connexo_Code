Ext.define('Mdc.service.Search', {
    extend: 'Uni.service.Search',

    init: function () {
        var me = this;
        me.defaultColumns = {
            'com.energyict.mdc.device.data.Device': ['id', 'name', 'serialNumber', 'deviceTypeName', 'deviceConfigurationName', 'state', 'location'],
            'com.elster.jupiter.metering.UsagePoint': ['name', 'displayServiceCategory', 'displayMetrologyConfiguration']
        };
        me.callParent(arguments);
    },

    createColumnDefinitionFromModel: function (field) {
        var me = this,
            column = this.callParent(arguments);

        if (column && column.dataIndex === 'name') {
            if (me.searchDomain.getId() === 'com.energyict.mdc.device.data.Device') {
                column.renderer = function (value, metaData, record) {
                    var url = me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(value)});
                    metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                    return '<a href="{0}">{1}</a>'.replace('{0}', url).replace('{1}', Ext.String.htmlEncode(value));
                };
            } else if (me.searchDomain.getId() === 'com.elster.jupiter.metering.UsagePoint') {
                column.renderer = function (value, metaData, record) {
                    var url = me.router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: encodeURIComponent(value)});
                    metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                    return '<a href="{0}">{1}</a>'.replace('{0}', url).replace('{1}', Ext.String.htmlEncode(value));
                };
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