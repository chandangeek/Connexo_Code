Ext.define('Mtr.view.workspace.issues.bulk.Navigation', {
    extend: 'Ext.view.View',
    alias: 'widget.bulk-navigation',

    requires: [
        'Ext.data.Store'
    ],

    tpl: new Ext.XTemplate(
        '<tpl for=".">',
        '<div class="x-bulk-action-item {active}">',
        '<tpl if="link">',
        '<a href="#">',
        '</tpl>',
        '<p>{#}- {name}</p>',
        '<tpl if="link">',
        '</a>',
        '</tpl>',
        '</div>',
        '</tpl>'
    ),

    itemSelector: 'div.x-bulk-action-item',

    initComponent: function () {
        Ext.apply(this, {
            store: Ext.create('Ext.data.Store', {
                fields: [
                    {name: 'name', type: 'string'},
                    {name: 'active', type: 'string'},
                    {name: 'link', type: 'boolean'}
                ],
                data: [
                    {name: 'Select issues', active: 'inactive', link: false},
                    {name: 'Select action', active: 'inactive', link: false},
                    {name: 'Action details', active: 'inactive', link: false},
                    {name: 'Confirmation', active: 'inactive', link: false},
                    {name: 'Status', active: 'inactive', link: false}
                ]
            })
        });

        this.callParent(arguments);
    },

    setActiveAction: function(index) {
        var store = this.getStore();
        store.each(function(record,idx){
            record.set('active', 'inactive');
            if(idx == index) {
                record.set('active', 'active-bulk-list-action');
            }
            record.commit();
        });
        this.refresh();
    },

    setHyperLinkAction: function(index, value) {
        var store = this.getStore();
        store.each(function(record,idx){
            if(idx == index) {
                record.set('link', value);
            }
            record.commit();
        });
        this.refresh();
    },

    clearHyperLinkActions: function() {
        var store = this.getStore();
        store.each(function(record,idx){
            record.set('link', false);
            record.commit();
        });
        this.refresh();
    },

    getActionsCount: function() {
        var store = this.getStore();
        return store.getTotalCount();
    }
});