Ext.define('Uni.view.toolbar.PagingBottom', {
    extend: 'Ext.toolbar.Paging',
    xtype: 'pagingtoolbarbottom',

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

    pageNavDelimiterTpl: new Ext.XTemplate('&nbsp;|&nbsp;'),
    pageNavItemTpl: new Ext.XTemplate('<a href="{1}">{0}</a>'),
    currentPageNavItemTpl: new Ext.XTemplate('<span>{0}</span>'),

    initComponent: function () {
        this.callParent(arguments);

        this.initPageSizeAndStartFromQueryString();

        var pagingCombo = this.child('#pagingCombo');
        pagingCombo.setRawValue('' + this.store.pageSize);
    },

    initPageSizeAndStartFromQueryString: function () {
        var queryString = this.getCurrentQueryString(),
            queryStrings = Ext.Object.fromQueryString(queryString),
            pageSize = queryStrings[this.pageSizeParam] || 10,
            pageStart = queryStrings[this.pageStartParam] || 0;

        this.initPageSizeAndStart(parseInt(pageSize, 10), parseInt(pageStart, 10));
    },

    initPageSizeAndStart: function (pageSize, pageStart) {
        pageStart = pageStart || 0;
        var pageNum = Math.max(Math.ceil((pageStart + 1) / pageSize), 1),
            changed = false;

        if (this.store.currentPage !== pageNum) {
            this.store.currentPage = pageNum;
            changed = true;
        }

        pageSize = pageSize || this.store.pageSize;
        pageSize = this.adjustPageSize(pageSize);
        if (this.store.pageSize !== pageSize) {
            this.store.pageSize = pageSize;
            changed = true;
        }

        if (changed) {
            this.resetQueryString();
            this.store.load();
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
            oldPage = me.store.currentPage,
            oldPageSize = me.store.pageSize,
            newPage = Math.floor(oldPage * oldPageSize / pageSize);

        this.store.currentPage = newPage;
        this.store.pageSize = pageSize;
        this.store.load();
    },

    resetQueryString: function (start) {
        var me = this,
            result = me.buildHrefWithQueryString(start),
            currentHref = location.href;

        if (currentHref !== result) {
            Ext.util.History.suspendEvents();

            Ext.TaskManager.start({
                run: function () {
                    if (location.href !== currentHref) {
                        Ext.util.History.resumeEvents();
                        this.stopped = true;
                    }
                },
                interval: 100
            });

            location.href = result;
        }
    },

    buildHrefWithQueryString: function (start) {
        var me = this,
            url = location.href.split('?')[0],
            queryString = me.buildQueryString(start);
        return url + '?' + queryString;
    },

    buildQueryString: function (start) {
        var me = this,
            queryString = me.getCurrentQueryString(),
            queryObject = Ext.Object.fromQueryString(queryString);

        start = start || me.getPageStartValue();
        Ext.apply(queryObject, {
            limit: me.store.pageSize,
            start: start
        });

        return Ext.Object.toQueryString(queryObject);
    },

    getPageStartValue: function (pageOffset) {
        var me = this,
            pageData = me.getPageData(),
            start = Math.max(pageData.fromRecord - 1, 0);

        pageOffset = pageOffset || 0;
        return start + me.store.pageSize * pageOffset;
    },

    getCurrentQueryString: function () {
        var token = Ext.util.History.getToken(),
            queryStringIndex = token.indexOf('?');
        return queryStringIndex < 0 ? '' : token.substring(queryStringIndex + 1);
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

            if (me.totalPages < pageCount) {
                me.totalPages = pageCount;
            }
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
            startPage = currPage - 5,
            endPage = currPage + 4,
            pageOffset,
            start;

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
                navItem.getEl().on('click', function () {
                    // History events are enabled again after the URL has been reset.
                    Ext.History.suspendEvents();
                    me.store.loadPage(page);
                });
            })
        }

        return navItem;
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
            href = me.buildHrefWithQueryString(start);

        return template.apply([page, href]);
    }

});