/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    usesExactCount: false,

    /**
     * @cfg {String}
     *
     *
     */
    displayMsg: Uni.I18n.translate('general.displayMsgItems', 'UNI', '{0} - {1} of {2} items'),

    /**
     * @cfg {String}
     */
    displayMoreMsg: Uni.I18n.translate('general.displayMsgMoreItems', 'UNI', '{0} - {1} of more than {2} items'),

    /**
     * @cfg {String}
     */
    emptyMsg: Uni.I18n.translate('general.noItemsToDisplay', 'UNI', 'There are no items to display'),

    isFullTotalCount: false,
    noBottomPaging: false,
    totalCount: -1,

    defaultButtonUI: 'default',

    exportButton: true,

    initComponent: function () {
        this.callParent(arguments);
    },

    getPagingItems: function () {
        return [
            {
                xtype: 'tbtext',
                itemId: 'displayItem'
            },
            '->',
            {
                xtype: 'exporterbutton',
                ui: 'icon',
                iconCls: 'icon-file-download',
                text: '',
                component: this.up('grid'),
                hidden: !this.exportButton
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
            if (me.usesExactCount) {
                me.totalCount = store.getTotalCount();
            } else {
                if (!!me.noBottomPaging) {
                    me.totalCount = store.getTotalCount();
                } else {
                    me.totalCount = me.totalCount <= store.getTotalCount() ? store.getTotalCount() : me.totalCount;
                }
            }
            if (store.getCount() === 0) {
                me.totalCount = -1;
                msg = me.emptyMsg;
            } else {
                totalCount = !!me.noBottomPaging ? me.totalCount : me.totalCount - 1;
                msg = me.displayMoreMsg;
                if (me.isFullTotalCount || store.pageSize * pageData.currentPage >= me.totalCount || me.usesExactCount) {
                    me.isFullTotalCount = true;
                    totalCount = me.totalCount;
                    msg = me.displayMsg;
                }
                var countFrom = pageData.fromRecord,
                    countTo = !!me.noBottomPaging ? store.getTotalCount() : pageData.toRecord,
                    countTotal = totalCount;
                if (countFrom === countTo && countTotal !== countFrom) {
                    countTotal = countFrom;
                }
                msg = Ext.String.format(msg, countFrom, countTo, countTotal);
            }
            displayItem.setText(msg);
        }
    },

    resetPaging: function () {
        var me = this;

        me.onLoad();
        me.totalCount = -1;
        me.isFullTotalCount = false;
    },

    onLoad: function () {
        Ext.suspendLayouts();
        this.updateInfo();
        Ext.resumeLayouts();

        this.fireEvent('change', this, this.getPageData());
    }
});