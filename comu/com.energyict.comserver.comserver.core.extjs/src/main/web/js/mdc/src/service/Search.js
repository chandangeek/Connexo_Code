Ext.define('Mdc.service.Search', {
    extend: 'Uni.service.Search',

    createColumnDefinitionFromModel: function (field) {
        var me = this,
            column = this.callParent(arguments);

        if (column && column.dataIndex === 'mRID') {
            if (me.searchDomain.getId() === 'com.energyict.mdc.device.data.Device') {
                column.renderer = function (value, metaData, record) {
                    var url = me.router.getRoute('devices/device').buildUrl({mRID: encodeURIComponent(value)});
                    metaData.tdAttr = 'data-qtip="' + value + '"';
                    return '<a href="{0}">{1}</a>'.replace('{0}', url).replace('{1}', Ext.String.htmlEncode(value));
                }
            } else if (me.searchDomain.getId() === 'com.elster.jupiter.metering.UsagePoint') {
                column.renderer = function (value, metaData, record) {
                    var url = me.router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: record.get('id')});
                    metaData.tdAttr = 'data-qtip="' + value + '"';
                    return '<a href="{0}">{1}</a>'.replace('{0}', url).replace('{1}', Ext.String.htmlEncode(value));
                }
            }
        }
        else {
            column.renderer = function (value, metaData, record) {
                metaData.tdAttr = 'data-qtip="' + value + '"';
                return value;
            }
        }

        return column;
    }
});