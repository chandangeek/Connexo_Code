/**
 * @class Uni.view.toolbar.PagingBottom
 */
Ext.define('Uni.view.toolbar.PagingBottom', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbarbottom',
    ui: 'pagingtoolbarbottom',

    defaultButtonUI: 'default',

    requires: [
        'Uni.util.QueryString',
        'Uni.util.History'
    ],

    /**
     * @cfg {Object} Query paramaters
     *
     * Query parameters to use when loading the store, e.g. for filtering or sorting.
     */
    params: {},

    /**
     * @cfg {Number} Default page size
     *
     * The default page size to use when initializing the paging component.
     */
    defaultPageSize: 10,

    totalCount: 0,
    totalPages: 0,
    isFullTotalCount: false,

    /**
     * @cfg {String} Limit parameter
     *
     * The limit parameter is used in the URL to define the amount of items that are visible per page.
     */
    pageSizeParam: 'limit',

    /**
     * @cfg {String} Start parameter
     *
     * The start parameter is used in the URL to define the start of the current paging options.
     */
    pageStartParam: 'start',

    /**
     * @cfg {Boolean} Lazy load
     *
     * Whether to load the store when the paging gets initialized or not.
     */
    lazyLoad: false,

    itemsPerPageMsg: Uni.I18n.translate('general.itemsPerPage', 'UNI', 'Items per page'),

    firstText: Uni.I18n.translate('general.firstPage', 'UNI', 'First page'),
    prevText: Uni.I18n.translate('general.previousPage', 'UNI', 'Previous page'),
    nextText: Uni.I18n.translate('general.nextPage', 'UNI', 'Next page'),
    lastText: Uni.I18n.translate('general.lastPage', 'UNI', 'Last page'),

    pageSizeStore: Ext.create('Ext.data.Store', {
        fields: ['value'],
        data: [
            {value: '10'},
            {value: '20'},
            {value: '50'},
            {value: '100'}
        ]
    }),

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

        pageSize = parseInt(pageSize, this.defaultPageSize) || this.store.pageSize;
        pageStart = parseInt(pageStart, this.defaultPageSize) || 0;

        this.initPageSizeAndStart(pageSize, pageStart);
    },

    initPageSizeAndStart: function (pageSize, pageStart) {
        var me = this,
            pageNum = Math.max(Math.ceil((pageStart + 1) / pageSize), 1);

        if (this.store.currentPage !== pageNum) {
            this.store.currentPage = pageNum;
        }

        pageSize = this.adjustPageSize(pageSize);
        if (this.store.pageSize !== pageSize) {
            this.store.pageSize = pageSize;
        }

        this.initExtraParams();

        if (!me.lazyLoad) {
            this.store.load({
                params: me.params,
                callback: function (records) {
                    if (records !== null && records.length === 0 && pageNum > 1) {
                        me.initPageSizeAndStart(pageSize, pageStart - pageSize);
                    }
                }
            });
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
        me.resetQueryString();
    },

    resetPageSize: function (pageSize) {
        var me = this,
            newPage = Math.max(Math.ceil((me.getPageStartValue() + 1) / pageSize), 1);

        me.store.currentPage = newPage;
        me.store.pageSize = pageSize;
        me.totalPages = 0;

        this.initExtraParams();

        me.store.load({
            params: me.params,
            callback: function (records) {
                if (records !== null && records.length === 0 && newPage > 1) {
                    me.initPageSizeAndStart(pageSize, pageStart - pageSize);
                }
            }
        });
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

        var obj = {};
        obj[me.pageSizeParam] = me.store.pageSize;
        obj[me.pageStartParam] = start;

        return Uni.util.QueryString.buildHrefWithQueryString(obj);
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
                text: me.itemsPerPageMsg
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
                flex: 1
            },
            {
                itemId: 'first',
                ui: 'gridnav',
                tooltip: me.firstText,
                overflowText: me.firstText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-first',
                disabled: true,
                handler: me.moveFirst,
                scope: me
            },
            {
                itemId: 'prev',
                ui: 'gridnav',
                tooltip: me.prevText,
                overflowText: me.prevText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-prev',
                disabled: true,
                handler: me.movePrevious,
                scope: me
            },
            {
                xtype: 'container',
                itemId: 'pageNavItem',
                cls: 'pagenav',
                layout: 'hbox'
            },
            {
                itemId: 'next',
                ui: 'gridnav',
                tooltip: me.nextText,
                overflowText: me.nextText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-next',
                disabled: true,
                handler: me.moveNext,
                scope: me
            },
            {
                itemId: 'last',
                ui: 'gridnav',
                tooltip: me.lastText,
                overflowText: me.lastText,
                iconCls: Ext.baseCSSPrefix + 'tbar-page-last',
                disabled: true,
                handler: me.moveLast,
                scope: me
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

        if (me.isFullTotalCount || (typeof pageData !== 'undefined' && me.store.pageSize * pageData.currentPage >= me.totalCount)) {
            me.setChildDisabled('#last', typeof pageData === 'undefined' || me.totalPages === pageData.currentPage);
            me.isFullTotalCount = true;
        }

        me.updateInfo();
        Ext.resumeLayouts(true);

        me.fireEvent('change', me, pageData);
    },

    moveLast: function () {
        var me = this,
            pageCount = me.getPageData().pageCount,
            last = pageCount < me.totalPages ? me.totalPages : pageCount;

        if (me.fireEvent('beforechange', me, last) !== false) {
            me.initExtraParams();

            me.store.loadPage(last, {
                params: me.params
            });

            me.resetQueryString();
            return true;
        }
        return false;
    },

    moveFirst: function () {
        var me = this;

        if (this.fireEvent('beforechange', this, 1) !== false) {
            me.initExtraParams();

            me.store.loadPage(1, {
                params: me.params
            });

            me.resetQueryString();
            return true;
        }
        return false;
    },

    movePrevious: function () {
        var me = this,
            store = me.store,
            prev = store.currentPage - 1;

        if (prev > 0) {
            if (me.fireEvent('beforechange', me, prev) !== false) {
                me.initExtraParams();

                store.loadPage(store.currentPage - 1, {
                    params: me.params
                });

                me.resetQueryString();
                return true;
            }
        }
        return false;
    },

    moveNext: function () {
        var me = this,
            store = me.store,
            total = me.getPageData().pageCount,
            next = store.currentPage + 1;

        if (next <= total) {
            if (me.fireEvent('beforechange', me, next) !== false) {
                me.initExtraParams();

                store.loadPage(store.currentPage + 1, {
                    params: me.params
                });

                me.resetQueryString();
                return true;
            }
        }
        return false;
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
        }
    },

    createPageNavItem: function (page, start, isCurrent) {
        var me = this,
            result = me.formatSinglePageNavItem(page, start, isCurrent),
            navItem = Ext.create('Ext.Component', {
                baseCls: Ext.baseCSSPrefix + 'toolbar-text',
                cls: isCurrent ? 'active' : '',
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
            me.initExtraParams();
            me.store.loadPage(page, {
                params: me.params,
                callback: function () {
                    var task = Ext.create('Ext.util.DelayedTask', function () {
                        Ext.History.resumeEvents();
                    });
                    task.delay(250);
                }
            });
        });
    },

    formatSinglePageNavItem: function (page, start, isCurrent) {
        var me = this,
            template = isCurrent ? me.currentPageNavItemTpl : me.pageNavItemTpl,
            href = me.buildQueryString(start);

        return template.apply([page, href]);
    },

    initExtraParams: function () {
        var me = this;
        if (Ext.isArray(me.params)) {
            me.params.forEach(function (entry) {
                var key = Object.keys(entry)[0];
                var value = entry[key];
                me.store.getProxy().setExtraParam(key, value);
            });
        }
    }

});