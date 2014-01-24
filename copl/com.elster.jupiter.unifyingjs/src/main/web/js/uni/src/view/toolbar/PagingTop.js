/**
 * @class Uni.view.toolbar.PagingTop
 */
Ext.define('Uni.view.toolbar.PagingTop', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbartop',

    displayInfo: false,
    displayMsg: '{0} - {1} of {2} items',
    displayMoreMsg: '{0} - {1} of more than {2} items',
    emptyMsg: 'There are no items to display',
    isFullTotalCount: false,
    totalCount: -1,

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