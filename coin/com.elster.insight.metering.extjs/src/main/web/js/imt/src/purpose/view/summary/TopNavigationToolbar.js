/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Imt.purpose.view.summary.TopNavigationToolbar', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.purpose-top-navigation-toolbar',
    layout: {
        type: 'hbox',
        pack: 'end'
    },

    store: null,
    pageSize: 4,
    page: 0,

    initComponent: function () {
        var me = this;

        me.items = ['->',
            {
                xtype: 'tbtext',
                itemId: 'displayItem',
                text: me.makeDisplayValue()
            },
            {
                itemId: 'previous-next-navigation-toolbar-previous-link',
                ui: 'plain',
                iconCls: 'uni-icon-arrow-up',
                style: 'margin-right: 0 !important;',
                scope: me,
                handler: me.previousPage
            },
            {
                itemId: 'previous-next-navigation-toolbar-next-link',
                ui: 'plain',
                iconCls: 'uni-icon-arrow-down',
                scope: me,
                handler: me.nextPage
            }];

        me.callParent(arguments);
        me.activateButtons();
    },

    nextPage: function () {
        this.page++;
        this.changePage();
    },

    previousPage: function () {
        this.page--;
        this.changePage();
    },

    changePage: function(pageNumber){
        var me = this;
        me.page = Ext.isNumber(pageNumber) ? pageNumber : me.page;
        me.down('#displayItem').setText(me.makeDisplayValue());
        me.activateButtons();
    },

    makeDisplayValue: function () {
        var me = this,
            store = Ext.getStore(me.store),
            storeTotal = store.getTotalCount(),
            fromCount = 1 + me.page * me.pageSize,
            toCount = storeTotal <= me.pageSize + me.page * me.pageSize ? storeTotal : me.pageSize + me.page * me.pageSize;

        return Uni.I18n.translate('general.displayMsgOutputs', 'IMT', '{0} - {1} of {2} outputs', [fromCount, toCount, storeTotal])
    },

    activateButtons: function () {
        var me = this,
            store = Ext.getStore(me.store),
            storeTotal = store.getTotalCount(),
            fromCount = 1 + me.page * me.pageSize,
            toCount = me.pageSize + me.page * me.pageSize;

        me.down('#previous-next-navigation-toolbar-next-link').setDisabled(storeTotal <= toCount);
        me.down('#previous-next-navigation-toolbar-previous-link').setDisabled(1 === fromCount);
        me.fireEvent('outputspagechanged', me.page, me.pageSize);
    }
});