/**
 * @class Uni.view.toolbar.PagingBottom
 */
Ext.define('Uni.view.toolbar.PagingBottom', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbarbottom',

    requires: [
        'Uni.util.QueryString',
        'Uni.util.History'
    ],

    totalCount: 0,
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

    pageNavDelimiterTpl: new Ext.XTemplate('|'),
    pageNavItemTpl: new Ext.XTemplate('<a href="{1}">{0}</a>'),
    currentPageNavItemTpl: new Ext.XTemplate('<span>{0}</span>'),

    initComponent: function () {
        this.callParent(arguments);

        this.initPageSizeAndStartFromQueryString();

        var pagingCombo = this.child('#pagingCombo');
        pagingCombo.setRawValue('' + this.store.pageSize);
    },

    initPageSizeAndStartFromQueryString: function () {
        var queryStrings = Uni.util.QueryString.getQueryStringValues(),
            pageSize = queryStrings[this.pageSizeParam],
            pageStart = queryStrings[this.pageStartParam];

        pageSize = parseInt(pageSize, 10) || this.store.pageSize;
        pageStart = parseInt(pageStart, 10) || 0;

        this.initPageSizeAndStart(pageSize, pageStart);
    },

    initPageSizeAndStart: function (pageSize, pageStart) {
        var me = this,
            pageNum = Math.max(Math.ceil((pageStart + 1) / pageSize), 1),
            changed = false;

        if (this.store.currentPage !== pageNum) {
            this.store.currentPage = pageNum;
            changed = true;
        }

        pageSize = this.adjustPageSize(pageSize);
        if (this.store.pageSize !== pageSize) {
            this.store.pageSize = pageSize;
            changed = true;
        }

        if (changed) {
            this.resetQueryString();
        }

        this.store.load({
            callback: function (records) {
                if (records !== null && records.length === 0 && pageNum > 1) {
                    me.initPageSizeAndStart(pageSize, pageStart - pageSize);
                }
            }
        });
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
        me.resetQueryString();
    },

    resetPageSize: function (pageSize) {
        var me = this,
            newPage = Math.max(Math.ceil((me.getPageStartValue() + 1) / pageSize), 1);

        me.store.currentPage = newPage;
        me.store.pageSize = pageSize;
        me.totalPages = 0;
        me.store.load();
    },

    resetQueryString: function (start) {
        var me = this,
            currentHref = location.href,
            result = me.buildQueryString(start);

        if (currentHref !== result) {
            Uni.util.History.suspendEventsForNextCall();
            location.href = result;
        }
    },

    buildQueryString: function (start) {
        var me = this;

        if (typeof start === 'undefined') {
            start = me.getPageStartValue();
        }

        return Uni.util.QueryString.buildHrefWithQueryString({
            limit: me.store.pageSize,
            start: start
        });
    },

    getPageStartValue: function (pageOffset) {
        var me = this,
            pageData = me.getPageData(),
            start = Math.max(pageData.fromRecord - 1, 0);

        pageOffset = pageOffset || 0;
        return start + me.store.pageSize * pageOffset;
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
                xtype: 'container',
                itemId: 'pageNavItem',
                layout: 'hbox'
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
            count,
            isEmpty,
            item;

        count = me.store.getCount();
        isEmpty = count === 0;
        if (!isEmpty) {
            pageData = me.getPageData();
            currPage = pageData.currentPage;
            pageCount = pageData.pageCount;

            me.totalCount = me.totalCount < me.store.getTotalCount() ? me.store.getTotalCount() : me.totalCount;
            me.totalPages = Math.ceil(me.totalCount / me.store.pageSize);
        } else {
            currPage = 0;
            pageCount = 0;
        }

        Ext.suspendLayouts();
        item = me.child('#pageNavItem');
        me.initPageNavItems(item, currPage, me.totalPages);

        me.setChildDisabled('#first', currPage === 1 || isEmpty);
        me.setChildDisabled('#prev', currPage === 1 || isEmpty);
        me.setChildDisabled('#next', currPage === pageCount || isEmpty);
        me.updateInfo();
        Ext.resumeLayouts(true);

        me.fireEvent('change', me, pageData);
        me.resetQueryString();
    },

    initPageNavItems: function (container, currPage, pageCount) {
        var me = this,
            pagesShowingCount = 10,
            startPage = Math.max(1, currPage - 5),
            endPage = Math.min(startPage + pagesShowingCount - 1, pageCount),
            pageOffset,
            start;

        if (endPage - startPage < pagesShowingCount - 1) {
            startPage = Math.max(1, endPage - pagesShowingCount + 1);
        }

        startPage = startPage < 1 ? 1 : startPage;
        endPage = endPage > pageCount ? pageCount : endPage;

        container.removeAll();
        for (var i = startPage; i <= endPage; i++) {
            pageOffset = i - currPage;
            start = me.getPageStartValue(pageOffset);
            container.add(me.createPageNavItem(i, start, pageOffset === 0));

            if (i < endPage) {
                container.add(me.createPageNavItemDelimiter());
            }
        }
    },

    createPageNavItem: function (page, start, isCurrent) {
        var me = this,
            result = me.formatSinglePageNavItem(page, start, isCurrent),
            navItem = Ext.create('Ext.Component', {
                baseCls: Ext.baseCSSPrefix + 'toolbar-text',
                html: result
            });

        if (!isCurrent) {
            navItem.on('afterrender', function () {
                me.addNavItemClickHandler(me, page, navItem);
            });
        }

        return navItem;
    },

    addNavItemClickHandler: function (me, page, navItem) {
        navItem.getEl().on('click', function () {
            Ext.History.suspendEvents();
            me.store.loadPage(page, {
                callback: function () {
                    Ext.History.resumeEvents();
                }
            });
        });
    },

    createPageNavItemDelimiter: function () {
        var me = this;
        return Ext.create('Ext.Component', {
            baseCls: Ext.baseCSSPrefix + 'toolbar-text',
            html: me.pageNavDelimiterTpl.apply()
        });
    },

    formatSinglePageNavItem: function (page, start, isCurrent) {
        var me = this,
            template = isCurrent ? me.currentPageNavItemTpl : me.pageNavItemTpl,
            href = me.buildQueryString(start);

        return template.apply([page, href]);
    }

});