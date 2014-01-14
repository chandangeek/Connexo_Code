Ext.define('Uni.view.toolbar.PagingBottom', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbarbottom',

    totalPages: 0,
    pageSizeParam: 'size',
    pageStartParam: 'start',
    pageSizeStore: Ext.create('Ext.data.Store', {
        fields: ['value'],
        data: [
            {value: '10'},
            {value: '20'},
            {value: '50'},
            {value: '100'}
        ]
    }),

    initComponent: function () {
        this.callParent(arguments);

        this.initPageSizeAndStartFromQueryString();

        var pagingCombo = this.child('#pagingCombo');
        pagingCombo.setRawValue('' + this.store.pageSize);

        this.on('beforechange', this.onBeforeChange, this);
    },

    initPageSizeAndStartFromQueryString: function () {
        var token = Ext.util.History.getToken(),
            queryStringIndex = token.indexOf('?'),
            queryString = token.substring(queryStringIndex + 1),
            queryStrings = Ext.Object.fromQueryString(queryString),
            pageSize = queryStrings[this.pageSizeParam],
            pageStart = queryStrings[this.pageStartParam];

        this.initPageSizeAndStart(parseInt(pageSize, 10), parseInt(pageStart, 10));
    },

    initPageSizeAndStart: function (pageSize, pageStart) {
        pageSize = pageSize || this.store.pageSize;
        pageSize = this.adjustPageSize(pageSize);
        this.store.pageSize = pageSize;

        pageStart = pageStart || 0;
        var pageNum = Math.max(Math.ceil((pageStart + 1) / pageSize), 1);
        this.store.loadPage(pageNum);
    },

    adjustPageSize: function (pageSize) {
        var match = pageSize,
            minDiff;

        this.pageSizeStore.each(function (record) {
            var value = parseInt(record.data.value, 10),
                diff = Math.abs(pageSize - value);

            if (diff < minDiff || typeof minDiff === 'undefined') {
                minDiff = diff;
                match = value;
            }
        });

        return match;
    },

    onBeforeChange: function (toolbar, page) {
        this.resetQueryString();
    },

    onPageSizeChange: function (combobox, value) {
        var me = this,
            pageSize = parseInt(value);
        me.resetPageSize(pageSize);
        me.resetQueryString();
    },

    resetPageSize: function (pageSize) {
        this.store.pageSize = pageSize;
        this.store.load();
    },

    resetQueryString: function () {
        var me = this,
            pageData = me.getPageData(),
            currPage = pageData.currentPage,
            pageCount = pageData.pageCount,
            start = me.store.pageSize * (currPage - 1),
            queryString = Ext.Object.toQueryString({
                size: me.store.pageSize,
                start: start
            });

        // TODO Set the page querystring.
    },

    getPagingItems: function () {
        var me = this;

        return [
            {
                xtype: 'tbtext',
                text: 'Items per page'
            },
            {
                xtype: 'combobox',
                itemId: 'pagingCombo',
                store: me.pageSizeStore,
                width: 64,
                queryMode: 'local',
                displayField: 'value',
                valueField: 'value',
                enableKeyEvents: true,
                keyNavEnabled: false,
                submitValue: false,
                isFormField: false,
                allowBlank: false,
                forceSelection: true,
                editable: false,
                scope: me,
                listeners: {
                    change: me.onPageSizeChange,
                    scope: me
                }
            },
            '-',
            {
                itemId: 'first',
                tooltip: me.firstText,
                overflowText: me.firstText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-first',
                disabled: true,
                handler: me.moveFirst,
                scope: me
            },
            {
                itemId: 'prev',
                tooltip: me.prevText,
                overflowText: me.prevText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-prev',
                disabled: true,
                handler: me.movePrevious,
                scope: me
            },
            '-',
            me.beforePageText,
            {
                xtype: 'numberfield',
                itemId: 'inputItem',
                name: 'inputItem',
                cls: Ext.baseCSSPrefix + 'tbar-page-number',
                allowDecimals: false,
                minValue: 1,
                hideTrigger: true,
                enableKeyEvents: true,
                keyNavEnabled: false,
                selectOnFocus: true,
                submitValue: false,
                isFormField: false,
                width: me.inputItemWidth,
                margins: '-1 2 3 2',
                listeners: {
                    scope: me,
                    keydown: me.onPagingKeyDown,
                    blur: me.onPagingBlur
                }
            },
            {
                xtype: 'tbtext',
                itemId: 'afterTextItem',
                text: Ext.String.format(me.afterPageText, 1)
            },
            '-',
            {
                itemId: 'next',
                tooltip: me.nextText,
                overflowText: me.nextText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-next',
                disabled: true,
                handler: me.moveNext,
                scope: me
            }
        ];
    },

    onLoad: function () {
        var me = this,
            pageData,
            currPage,
            pageCount,
            afterText,
            count,
            isEmpty,
            item;

        count = me.store.getCount();
        isEmpty = count === 0;
        if (!isEmpty) {
            pageData = me.getPageData();
            currPage = pageData.currentPage;
            pageCount = pageData.pageCount;

            // Check for invalid current page.
            if (currPage > pageCount) {
                me.store.loadPage(pageCount);
                return;
            }

            if (me.totalPages < pageCount) {
                me.totalPages = pageCount;
            }

            afterText = Ext.String.format(me.afterPageText, isNaN(me.totalPages) ? 1 : me.totalPages);
        } else {
            currPage = 0;
            pageCount = 0;
            afterText = Ext.String.format(me.afterPageText, 0);
        }

        Ext.suspendLayouts();
        item = me.child('#afterTextItem');
        if (item) {
            item.setText(afterText);
        }
        item = me.getInputItem();
        if (item) {
            item.setDisabled(isEmpty).setValue(currPage);
        }
        me.setChildDisabled('#first', currPage === 1 || isEmpty);
        me.setChildDisabled('#prev', currPage === 1 || isEmpty);
        me.setChildDisabled('#next', currPage === pageCount || isEmpty);
        me.setChildDisabled('#last', currPage === pageCount || isEmpty);
        me.setChildDisabled('#refresh', false);
        me.updateInfo();
        Ext.resumeLayouts(true);

        me.fireEvent('change', me, pageData);
    }

});