Ext.define('Imt.registerdata.view.RegisterList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerList',
    requires: [
        'Imt.registerdata.store.Register',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.registerdata.view.ActionMenu'
    ],
    store: 'Imt.registerdata.store.Register',
    mRID: null,
    scroll: false,
 //   overflowY: 'auto',
    itemId: 'registerList',
    title: Uni.I18n.translate('registers.registerList', 'IMT', 'Registers'),
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true

    },
    initComponent: function () {
        var me = this;
        me.columns = [
        {
            header: Uni.I18n.translate('registers.title.registers', 'IMT', 'Registers'),
            flex: 1,
            dataIndex: 'readingTypeFullAliasName', 
            renderer: function (value, b, record) {
                var me = this,
                   url = me.router.getRoute('usagepoints/view/registers/register').buildUrl({mRID: me.mRID, registerId: record.get('readingTypemRID')});//record.get('id')});

                return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
            }
        },
        {
            header: Uni.I18n.translate('registers.title.registers', 'IMT', 'Last reading timestamp'),
            flex: 1,
            dataIndex: 'lastValueTimestamp', 
            renderer: function(value){
                if(!Ext.isEmpty(value)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(value));
                }
                return '-';
            },
        },
        {
            header: Uni.I18n.translate('registers.title.registers', 'IMT', 'Last reading value'),
            flex: 1,
            dataIndex: 'lastReadingValue', 
        }
        ];

        me.callParent(arguments);
    }
});