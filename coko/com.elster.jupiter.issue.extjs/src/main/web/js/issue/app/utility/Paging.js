Ext.define('ViewDataCollectionIssues.utility.Paging', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.pagingpanel',
    alternateClassName: 'Ext.PagingPanel',
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
        } else {
            currPage = 0;
            pageCount = 0;
        }

        self.child('#pagesnavigation').child('#prev').setDisabled(currPage === 1);
        self.child('#pagesnavigation').child('#next').setDisabled(isEmpty);
        self.child('#selectPerPage').setDisabled(isEmpty);
        this.child('#pagesnavigation').child('#currentpage').removeAll();
        this.child('#pagesnavigation').child('#currentpage').add({
            xtype: 'container',
            html: currPage
        });

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
            margin: '0 170 0 0',
            layout: {
                type: 'hbox',
                pack: 'center'
            },
            items: [{
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
                itemId: 'currentpage',
                margin: '3 10 0 10'
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
            }]
        }];
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
        var self = this;

        if (self.fireEvent('beforechange', self) !== false) {
            self.store.nextPage();
        }
    },

    onChangePerPage: function (combobox, value) {
        if (this.fireEvent('beforechange', this, 1) !== false){
            this.store.pageSize = value;
            this.store.loadPage(1);
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
