Ext.define('Mtr.widget.Breadcrumbs', {
    extend: 'Ext.Component',
    alias: 'widget.breadcrumbs',

    baseCls: 'breadcrumbs',

    items: [],

    renderTpl: [
        '<ul id="{id}-wrap">',
        '<tpl for="items">',
        '<li class="item">',
        '<tpl if="href">',
        '<a href="{href}"',
        '<tpl if="hrefTarget">',
        ' target="{hrefTarget}"',
        '</tpl>',
        '>',
        '</tpl>',
        '{label}',
        '<tpl if="href">',
        '</a>',
        '</tpl>',
        '</li>',
        '<tpl if="xindex < xcount">',
        '<li class="delimiter"></li>',
        '</tpl>',
        '</tpl>',
        '</ul>'
    ],

    initComponent: function () {
        this.callParent();
    },

    beforeRender: function () {
        this.callParent();

        // Apply the renderData to the template args
        Ext.applyIf(this.renderData, this.getTemplateArgs());
    },

    getTemplateArgs: function () {
        return {
            id: this.id,
            baseCls: this.baseCls,
            items: this.items
        };
    },

    add: function () {
        var me = this,
            args = Ext.Array.slice(arguments),
            addingArray,
            items;

        if (args.length == 1 && Ext.isArray(args[0])) {
            items = args[0];
            addingArray = true;
        } else {
            items = args;
        }

        if (me.rendered) {
            Ext.suspendLayouts(); // suspend layouts while adding items...
        }

        if (addingArray) {
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                me.items.push(item);
            }
        } else {
            me.items.push(items);
        }

        var template = Ext.XTemplate(me.renderTpl);
        me.update(template.apply(me.getTemplateArgs()));

        me.updateLayout();
        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },
    removeAll: function () {
        var me = this;

        if (me.rendered) {
            Ext.suspendLayouts(); // suspend layouts while adding items...
        }

        me.items = [];

        me.updateLayout();
        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    }
});