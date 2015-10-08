/**
 * @class MdcApp.controller.Main
 */
Ext.define('Mdc.controller.Search', {
    extend: 'Uni.controller.Search',

    init: function () {
        var me = this, url,
            router = this.getController('Uni.controller.history.Router');

        me.callParent(arguments);
        me.on('beforegridconfigure', function(fields, columns){
            var cid = _.pluck(columns, 'dataIndex').indexOf('mRID');
            var column = columns[cid];
            if (column) {
                column.renderer = function (value) {
                    if (me.searchDomain.get('id') == 'com.elster.jupiter.metering.UsagePoint') {
                        url = router.getRoute('usagepoints/usagepoint').buildUrl({usagePointMRID: value});
                    } else {
                        url = router.getRoute('devices/device').buildUrl({mRID: value});
                    }
                    return '<a href="{0}">{1}</a>'.replace('{0}', url).replace('{1}', Ext.String.htmlEncode(value));
                }
            }
        })
    }
});
