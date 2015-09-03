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
    overflowY: 'auto',
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
            dataIndex: 'readingTypeAlias', 
            renderer: function (value, b, record) {
                var me = this,
                    url = me.router.getRoute('administration/usagepoint/register').buildUrl({mRID: me.mRID, register: record.get('readingTypemRID')});//record.get('id')});

                return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
            }
        },
        {
            header: Uni.I18n.translate('registers.title.readingTimestamp', 'IMT', 'Reading Timestamp'),
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
            header: Uni.I18n.translate('registers.title.readingValue', 'IMT', 'Reading Value'),
            flex: 1,
            dataIndex: 'readingValue', 
        },
        {
            header: Uni.I18n.translate('registers.title.recordedTimestamp', 'IMT', 'Recorded Timestamp'),
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
//                  displayMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} registers'),
//                  displayMoreMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} registers'),
//                  emptyMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.emptyMsg', 'IMT', 'There are no registers')
//              },
//              {
//                  xtype: 'pagingtoolbarbottom',
//                  store: me.store,
//                  params: [
//                      {mRID: me.mRID}
//                  ],
//                  itemsPerPageMsg: Uni.I18n.translate('registerdata.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Register configurations per page'),
//                  dock: 'bottom'
//              }
//          ];
        me.callParent(arguments);
    }
});