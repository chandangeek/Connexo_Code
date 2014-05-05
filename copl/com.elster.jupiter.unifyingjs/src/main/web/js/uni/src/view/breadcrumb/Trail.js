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
        this.removeAll();
        this.addBreadcrumbItem(item);
    },

    addBreadcrumbItem: function (item, baseHref) {
        // TODO Append '#/' when necessary.
        baseHref = baseHref || '';

        if (item.data.relative && baseHref.length > 0) {
            baseHref += Uni.controller.history.Settings.tokenDelimiter;
        }

        var child,
            href = item.data.href,
            link = Ext.widget('breadcrumbLink', {
                text: item.data.text
            });

        try {
            child = item.getChild();
        } catch (ex) {
            // Ignore.
        }

        if (child !== undefined && child.rendered) {
            link.setHref(baseHref + href);
        } else if (child !== undefined && !child.rendered) {
            link.href = baseHref + href;
        }

        this.addBreadcrumbComponent(link);

        // Recursively add the children.
        if (child !== undefined && child !== null) {
            if (item.data.relative) {
                baseHref += href;
            }

            this.addBreadcrumbItem(child, baseHref);
        }
    },

    addBreadcrumbComponent: function (component) {
        var itemCount = this.items.getCount();

        if (itemCount % 2 === 1) {
            this.add(Ext.widget('breadcrumbSeparator'));
        }

        this.add(component);
    }
});