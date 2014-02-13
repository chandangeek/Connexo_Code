Ext.define('ViewDataCollectionIssues.utility.Pagination', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.paginationtoolbar',
    alternateClassName: 'Ext.PaginationToolbar',
    requires: [
        'Ext.form.field.ComboBox'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable'    
    },
    firstText : 'First Page',
    prevText : 'Previous Page',
    nextText : 'Next Page',
    lastText : 'Last Page',
    perPageText: 'Per page',
    perPageValues: [10,20,30,40,50,100],
    
    initComponent: function () {
        this.items = this.getItems();
        this.callParent();
        this.addEvents('change','beforechange');
        this.on('beforerender', this.onLoad, this, {single: true});
        this.bindStore(this.store || 'ext-empty-store', true);
    },

    onLoad: function () {
        var self = this,
            pageData,
            currPage,
            pageCount,
            count,
            isEmpty;

        count = self.store.getCount();
        isEmpty = count === 0;
        if (!isEmpty) {
            pageData = self.getPageData();
            currPage = pageData.currentPage;
            pageCount = pageData.pageCount;
            self.buildPagination();
            self.resizePagination();
        } else {
            currPage = 0;
            pageCount = 0;
        }

        self.child('#pagesnavigation').child('#first').setDisabled(currPage === 1 || isEmpty);
        self.child('#pagesnavigation').child('#prev').setDisabled(currPage === 1 || isEmpty);
        self.child('#pagesnavigation').child('#next').setDisabled(currPage === pageCount  || isEmpty);
        self.child('#pagesnavigation').child('#last').setDisabled(currPage === pageCount  || isEmpty);
        self.child('#selectPerPage').setDisabled(isEmpty);

        if (self.rendered) {
            self.fireEvent('change', self, pageData);
        }
    },

    getStoreListeners: function() {
        return {
            load: this.onLoad
        };
    },

    getItems: function () {
        var self         = this,
            perPagesData = [],
            perPages;
            

        Ext.Array.each(this.perPageValues, function (value) {
            perPagesData.push({'number':value})
        });

        perPages = Ext.create('Ext.data.Store', {
            fields: ['number'],
            data : perPagesData
        });

        return [{
            xtype: 'combobox',
            itemId: 'selectPerPage',
            width: 170,
            fieldLabel: self.perPageText,
            store: perPages,
            queryMode: 'local',
            editable: false,
            displayField: 'number',
            valueField: 'number',
            listeners: {
                beforerender: function (combobox) {
                    combobox.setValue(combobox.store.getAt(0));
                    self.store.pageSize = combobox.getValue();
                    combobox.on('change', self.onChangePerPage, self);
                }
            }
        },{
            xtype: 'container',
            itemId: 'pagesnavigation',
            flex: 1,
            layout: {
                type: 'hbox',
                pack: 'center'
            },
            items: [{
                xtype: 'button',
                itemId: 'first',
                tooltip: self.firstText,
                overflowText: self.firstText,
                cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-medium',
                iconCls: Ext.baseCSSPrefix + 'tbar-page-first',
                disabled: true,
                handler: self.moveFirst,
                scope: self
            },{
                xtype: 'button',
                itemId: 'prev',
                tooltip: self.prevText,
                overflowText: self.prevText,
                cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-medium',
                iconCls: Ext.baseCSSPrefix + 'tbar-page-prev',
                disabled: true,
                handler: self.movePrevious,
                scope: self
            },{
                xtype: 'container',
                itemId: 'pagination',
                margin: '0 10 0 10',
                height: 20
            },{
                xtype: 'button',
                itemId: 'next',
                tooltip: self.nextText,
                overflowText: self.nextText,
                cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-medium',
                iconCls: Ext.baseCSSPrefix + 'tbar-page-next',
                disabled: true,
                handler: self.moveNext,
                scope: self
            },{
                xtype: 'button',
                itemId: 'last',
                tooltip: self.lastText,
                overflowText: self.lastText,
                cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-medium',
                iconCls: Ext.baseCSSPrefix + 'tbar-page-last',
                disabled: true,
                handler: self.moveLast,
                scope: self,
                margin: '0 160 0 0'
            }]
        }];
    },

    buildPagination: function () {
        var self       = this,
            pageData   = self.getPageData(),
            btns       = [],
            totalPages = pageData.pageCount,
            curentPage = pageData.currentPage,
            start      = Math.floor(curentPage / 10) ? curentPage - 5 : 1,
            minPage    = (totalPages - 10) > 0 ? totalPages - 10 : 1,
            limit      = start == 1 ? 10 : curentPage + 4,
            pageBtn;

        for (var i = (curentPage + 4 < totalPages ? start : minPage); i <= (limit < totalPages ? limit : totalPages); i++) {
            pageBtn = Ext.create('Ext.button.Button',{
                text: i,
                toPage: i,
                disabled: i == pageData.currentPage,
                cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-small',
                handler: self.goToPage,
                scope: self,
                margin: 0
            });

            btns.push(pageBtn);
        }

        this.child('#pagesnavigation').child('#pagination').removeAll();
        this.child('#pagesnavigation').child('#pagination').add(btns);
    },

    resizePagination: function () {
        var pagination = this.child('#pagesnavigation').child('#pagination'),
            paginationWidth = 0;

        Ext.Array.each(pagination.items.getRange(0, 10), function (item) {
            paginationWidth += item.getWidth();
        });

        pagination.setWidth(paginationWidth);
    },

    getPageData : function(){
        var store = this.store,
            totalCount = store.getTotalCount();

        return {
            total : totalCount,
            currentPage : store.currentPage,
            pageCount: Math.ceil(totalCount / store.pageSize),
            fromRecord: ((store.currentPage - 1) * store.pageSize) + 1,
            toRecord: Math.min(store.currentPage * store.pageSize, totalCount)
        };
    },

    moveFirst : function(){
        if (this.fireEvent('beforechange', this, 1) !== false){
            this.store.loadPage(1);
        }
    },

    movePrevious : function(){
        var self = this,
            prev = self.store.currentPage - 1;

        if (prev > 0) {
            if (self.fireEvent('beforechange', self, prev) !== false) {
                self.store.previousPage();
            }
        }
    },

    moveNext : function(){
        var self = this,
            total = self.getPageData().pageCount,
            next = self.store.currentPage + 1;

        if (next <= total) {
            if (self.fireEvent('beforechange', self, next) !== false) {
                self.store.nextPage();
            }
        }
    },

    moveLast : function(){
        var self = this,
            last = self.getPageData().pageCount;

        if (self.fireEvent('beforechange', self, last) !== false) {
            self.store.loadPage(last);
        }
     },

    onChangePerPage: function (combobox, value) {
        if (this.fireEvent('beforechange', this, 1) !== false){
            this.store.pageSize = value;
            this.store.loadPage(1);
        }
    },

    goToPage: function (btn) {
        if (this.fireEvent('beforechange', this, 1) !== false){
            this.store.loadPage(btn.toPage);
        }
    },

    unbind : function(store){
        this.bindStore(null);
    },

    bind : function(store){
        this.bindStore(store);
    },

    onDestroy : function(){
        this.unbind();
        this.callParent();
    }
});
