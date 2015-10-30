Ext.define('Uni.view.search.field.internal.CriteriaPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-search-internal-criteriapanel',
    config: {
        service: null,
        sticky: false
    },
    flex: 1,
    defaults: {
        margin: '0 10 10 0'
    },
    layout: 'column',

    initComponent: function() {
        var service = this.getService();

        service.on('reset', this.onReset, this);
        service.on('add', this.onCriteriaAdd, this);
        service.on('remove', this.onCriteriaRemove, this);
        this.callParent(arguments);
    },

    onReset: function() {
        this.removeAll();
        this.setVisible(false);
    },

    onCriteriaAdd: function(filters, filter, property) {
        if (property.get('sticky') === this.sticky) {
            this.add(filter);
            this.setVisible(this.items.length);
        }
    },

    onCriteriaRemove: function(filters, filter, property) {
        if (property.get('sticky') === this.sticky) {
            this.remove(filter);
            this.setVisible(this.items.length);
        }
    }
});