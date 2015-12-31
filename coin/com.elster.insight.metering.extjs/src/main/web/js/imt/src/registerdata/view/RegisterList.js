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
//    title: Uni.I18n.translate('registerdata.label.register.list', 'IMT', 'Registers'),
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true

    },
    initComponent: function () {
        var me = this;
        me.columns = [
	        {
	            header: Uni.I18n.translate('registerdata.label.registers', 'IMT', 'Registers'),
	            flex: 1,
	            dataIndex: 'readingTypeFullAliasName', 
	            renderer: function (value, b, record) {
	                var me = this,
	                   url = me.router.getRoute('usagepoints/view/registers/registerdata').buildUrl({mRID: me.mRID, registerId: record.get('readingTypemRID')});//record.get('id')});
	
	                return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
	            }
	        },
	        {
	            header: Uni.I18n.translate('registerdata.label.lastreading.timestamp', 'IMT', 'Last reading timestamp'),
	            flex: 1,
	            dataIndex: 'lastValueTimestamp', 
	            renderer: function(value){
	                if(!Ext.isEmpty(value)) {
	                    return Uni.DateTime.formatDateTimeLong(new Date(value));
	                }
	                return '-';
	            }
	        },
	        {
	            header: Uni.I18n.translate('registerdata.label.lastreading.value', 'IMT', 'Last reading value'),
	            flex: 1,
	            dataIndex: 'lastReadingValue'
	        },
	        {
	            xtype: 'uni-actioncolumn',
	            menu: {
	                xtype: 'registerActionMenu',
	                itemId: 'registerActionMenu'
	            }
	        }
	    ];
	
	    me.dockedItems = [
	        {
	            xtype: 'pagingtoolbartop',
	            usesExactCount: true,
	            store: me.store,
	            dock: 'top',
	            displayMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} registers'),
	            displayMoreMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} registers'),
	            emptyMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.emptyMsg', 'IMT', 'There are no registers to display')
	        },
	        {
	            xtype: 'pagingtoolbarbottom',
	            store: me.store,
	            itemsPerPageMsg: Uni.I18n.translate('registerdata.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Registers per page'),
	            dock: 'bottom',
	            deferLoading: true
	        }
	    ];

        me.callParent(arguments);
    }
});