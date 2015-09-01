Ext.define('Imt.registerdata.view.ReadingList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.readingList',
    requires: [
        'Imt.registerdata.store.Reading',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.registerdata.view.ActionMenu'
    ],
    store: 'Imt.registerdata.store.Reading',
    overflowY: 'auto',
    itemId: 'readingList',
    title: Uni.I18n.translate('registers.readingList', 'IMT', 'Readings'),
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true

    },
    initComponent: function () {
        var me = this;
        me.columns = [
//        {
//            header: Uni.I18n.translate('registers.title.registers', 'MDC', 'Readings'),
//            flex: 1,
//            dataIndex: 'readingTypeAlias', 
//            renderer: function (value, b, record) {
//                var me = this,
//                    url = me.router.getRoute('administration/usagepoint/reading').buildUrl({mRID: me.mRID, register: record.get('readingTypemRID')});//record.get('id')});
//
//                return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
//            }
//        },
        {
            header: Uni.I18n.translate('registers.title.registers', 'MDC', 'Reading Timestamp'),
            flex: 1,
            dataIndex: 'utcTimestamp', 
            renderer: function(value){
                if(!Ext.isEmpty(value)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(value));
                }
                return '-';
            },
        },
        {
            header: Uni.I18n.translate('registers.title.registers', 'MDC', 'Reading Value'),
            flex: 1,
            dataIndex: 'readingValue', 
        },
        {
            header: Uni.I18n.translate('registers.title.registers', 'MDC', 'Recorded Timestamp'),
            flex: 1,
            dataIndex: 'recordedTime', 
            renderer: function(value){
                if(!Ext.isEmpty(value)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(value));
                }
                return '-';
            },
        },
//        {
//            xtype: 'uni-actioncolumn',
//            menu: {
//                xtype: 'registerActionMenu',
//                itemId: 'registerActionMenu'
//            }
//        }
    ];
//        me.dockedItems = [
//              {
//                  xtype: 'pagingtoolbartop',
//                  store: me.store,
//                  dock: 'top',
//                  displayMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} registers'),
//                  displayMoreMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} registers'),
//                  emptyMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.emptyMsg', 'MDC', 'There are no registers')
//              },
//              {
//                  xtype: 'pagingtoolbarbottom',
//                  store: me.store,
//                  params: [
//                      {mRID: me.mRID}
//                  ],
//                  itemsPerPageMsg: Uni.I18n.translate('registerdata.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register configurations per page'),
//                  dock: 'bottom'
//              }
//          ];
        me.callParent(arguments);
    }
});