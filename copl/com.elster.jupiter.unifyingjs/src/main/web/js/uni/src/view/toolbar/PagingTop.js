Ext.define('Uni.view.toolbar.PagingTop', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbartop',

    displayInfo: false,
    displayMsg: '{0} - {1} of {2} items',
    displayMoreMsg: '{0} - {1} of more than {2} items',
    emptyMsg: 'There are no items to display',
    totalCount: 0,

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
            count,
            totalCount,
            msg;

        if (displayItem) {
            count = store.getCount();
            me.totalCount = me.totalCount < store.getTotalCount() ? store.getTotalCount() : me.totalCount;

            if (count === 0) {
                msg = me.emptyMsg;
            } else {
                msg = me.displayMsg;

                if (count < me.totalCount) {
                    msg = me.displayMoreMsg;
                    totalCount = me.totalCount - 1;
                } else {
                    totalCount = count;
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
    }
});