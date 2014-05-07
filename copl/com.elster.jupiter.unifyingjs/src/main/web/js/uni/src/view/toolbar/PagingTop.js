/**
 * @class Uni.view.toolbar.PagingTop
 *
 *  this.dockedItems = [
 {
     xtype: 'pagingtoolbartop',
     store: this.store,
     dock: 'top',
     items: [
         {
             xtype: 'component',
             flex: 1
         },
         {
             text: 'Create device type',
             itemId: 'createDeviceType',
             xtype: 'button',
             action: 'createDeviceType'
         },
         {
             text: 'Bulk action',
             itemId: 'deviceTypesBulkAction',
             xtype: 'button'
         }
     ]
 },
 {
 xtype: 'pagingtoolbarbottom',
 store: this.store,
 dock: 'bottom'
}];
 */
Ext.define('Uni.view.toolbar.PagingTop', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbartop',
    ui: 'pagingtoolbartop',

    displayInfo: false,

    /**
     * @cfg {String}
     *
     *
     */
    displayMsg: '{0} - {1} of {2} items',

    /**
     * @cfg {String}
     */
    displayMoreMsg: '{0} - {1} of more than {2} items',

    /**
     * @cfg {String}
     */
    emptyMsg: 'There are no items to display',

    isFullTotalCount: false,
    totalCount: -1,

    defaultButtonUI: 'default',

    initComponent: function () {
        this.callParent(arguments);
    },

    getPagingItems: function () {
        return [
            {
                xtype: 'tbtext',
                itemId: 'displayItem'
            }
        ];
    },

    updateInfo: function () {
        var me = this,
            displayItem = me.child('#displayItem'),
            store = me.store,
            pageData = me.getPageData(),
            totalCount,
            msg;

        if (displayItem) {
            me.totalCount = me.totalCount < store.getTotalCount() ? store.getTotalCount() : me.totalCount;

            if (store.getCount() === 0) {
                me.totalCount = -1;
                msg = me.emptyMsg;
            } else {
                totalCount = me.totalCount - 1;
                msg = me.displayMoreMsg;

                if (me.isFullTotalCount || store.pageSize * pageData.currentPage >= me.totalCount) {
                    me.isFullTotalCount = true;
                    totalCount = me.totalCount;
                    msg = me.displayMsg;
                }

                msg = Ext.String.format(
                    msg,
                    pageData.fromRecord,
                    pageData.toRecord,
                    totalCount
                );
            }
            displayItem.setText(msg);
        }
    },

    onLoad: function () {
        Ext.suspendLayouts();
        this.updateInfo();
        Ext.resumeLayouts(true);

        this.fireEvent('change', this, this.getPageData());
    }
});