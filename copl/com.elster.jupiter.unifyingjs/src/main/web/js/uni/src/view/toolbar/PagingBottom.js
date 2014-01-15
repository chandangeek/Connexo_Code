Ext.define('Uni.view.toolbar.PagingBottom', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbarbottom',

    currStart: 0,
    totalPages: 0,
    pageSizeParam: 'limit',
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
    },
    initPageSizeAndStartFromQueryString: function () {
        var queryString = this.getCurrentQueryString(),
            queryStrings = Ext.Object.fromQueryString(queryString),
            pageSize = queryStrings[this.pageSizeParam],
            pageStart = queryStrings[this.pageStartParam];

        this.initPageSizeAndStart(parseInt(pageSize, 10), parseInt(pageStart, 10));
    },

    initPageSizeAndStart: function (pageSize, pageStart) {
        pageSize = pageSize || this.store.pageSize;
        pageSize = this.adjustPageSize(pageSize);
        this.store.pageSize = pageSize;

        this.currStart = pageStart || 0;
        var pageNum = Math.max(Math.ceil((this.currStart + 1) / pageSize), 1);

        if (this.store.currentPage !== pageNum) {
            this.store.currentPage = pageNum;
            this.store.loadPage(pageNum);
        }
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

    onPageSizeChange: function (combobox, value) {
        var me = this,
            pageSize = parseInt(value, 10);

        me.resetPageSize(pageSize);
    },

    resetPageSize: function (pageSize) {
        this.store.pageSize = pageSize;
        this.store.load();
    },

    resetQueryString: function () {
        var me = this,
            url = location.href.split('?')[0],
            queryString = me.buildQueryString(),
            result = url + '?' + queryString;

        if (location.href !== result) {
            location.href = result;
        }
    },

    buildQueryString: function (start) {
        var me = this,
            pageData = me.getPageData(),
            queryString = me.getCurrentQueryString(),
            queryObject = Ext.Object.fromQueryString(queryString);

        start = (Math.floor(pageData.toRecord / me.store.pageSize) - 1) * me.store.pageSize;
        Ext.apply(queryObject, {
            limit: me.store.pageSize,
            start: start
        });

        return Ext.Object.toQueryString(queryObject);
    },

    getCurrentQueryString: function () {
        var token = Ext.util.History.getToken(),
            queryStringIndex = token.indexOf('?');
        return token.substring(queryStringIndex + 1);
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
            {
                xtype: 'component',
                html: '&nbsp;',
                flex: 0.7
            },
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
            {
                xtype: 'tbtext',
                itemId: 'pageNavItem'
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
            },
            {
                xtype: 'component',
                html: '&nbsp;',
                flex: 1
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

            if (me.totalPages < pageCount) {
                me.totalPages = pageCount;
            }
        } else {
            currPage = 0;
            pageCount = 0;
        }

        Ext.suspendLayouts();
        item = me.child('#pageNavItem');
        item.setText(me.formatPageNavItemText(currPage, me.totalPages));

        me.setChildDisabled('#first', currPage === 1 || isEmpty);
        me.setChildDisabled('#prev', currPage === 1 || isEmpty);
        me.setChildDisabled('#next', currPage === pageCount || isEmpty);
        me.updateInfo();
        Ext.resumeLayouts(true);

        me.fireEvent('change', me, pageData);
        me.resetQueryString();
    },

    formatPageNavItemText: function (currPage, pageCount) {
        var me = this,
            querystring = me.buildQueryString();

        // TODO Add the querystring to each page link except for the current page.
        return querystring;
    }

});