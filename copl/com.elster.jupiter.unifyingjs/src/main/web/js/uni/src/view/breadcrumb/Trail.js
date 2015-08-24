/**
 * @class Uni.view.breadcrumb.Trail
 */
Ext.define('Uni.view.breadcrumb.Trail', {
    extend: 'Ext.container.Container',
    alias: 'widget.breadcrumbTrail',
    ui: 'breadcrumbtrail',

    requires: [
        'Uni.view.breadcrumb.Link',
        'Uni.view.breadcrumb.Separator',
        'Uni.controller.history.Settings'
    ],

    layout: {
        type: 'hbox',
        align: 'middle'
    },

    setBreadcrumbItem: function (item) {
        var me = this;

        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.removeAll();
        me.addBreadcrumbItem(item);

        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    },

    addBreadcrumbItem: function (item, baseHref) {
        var me = this,
            isGrandParent = false,
            child,
            href = item.data.href,
            link = Ext.create('Uni.view.breadcrumb.Link', {
                key: item.get('key'),
                text: item.get('text')
            });

        // TODO Append '#/' when necessary.
        if (!baseHref) {
            isGrandParent = true;
            baseHref = baseHref || '';
        }

        if (item.data.relative && baseHref.length > 0) {
            baseHref += Uni.controller.history.Settings.tokenDelimiter;
        }

        try {
            child = item.getChild();
        } catch (ex) {
            // Ignore.
        }

        if (typeof child !== 'undefined' && child !== null && child.rendered) {
            link.setHref(baseHref + href);
        } else if (typeof child !== 'undefined' && child !== null && !child.rendered) {
            link.href = baseHref + href;
        }

        me.addBreadcrumbComponent(link);
        me[me.items.length > 1 ? 'show' : 'hide']();

        // Recursively add the children.
        if (typeof child !== 'undefined' && child !== null) {
            if (item.data.relative) {
                baseHref += href;
            }

            me.addBreadcrumbItem(child, baseHref);
        }
    },

    addBreadcrumbComponent: function (component) {
        var itemCount = this.items.getCount();
        Ext.suspendLayouts();
        if (itemCount % 2 === 1) {
            this.add(Ext.widget('breadcrumbSeparator'));
        }

        this.add(component);
        Ext.resumeLayouts(true);
    }
});