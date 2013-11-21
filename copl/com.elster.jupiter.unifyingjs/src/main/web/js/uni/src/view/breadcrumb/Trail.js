Ext.define('Uni.view.breadcrumb.Trail', {
    extend: 'Ext.container.Container',
    alias: 'widget.breadcrumbTrail',

    requires: [
        'Uni.view.breadcrumb.Link',
        'Uni.view.breadcrumb.Separator'
    ],

    layout: {
        type: 'hbox',
        align: 'middle'
    },

    setBreadcrumbItem: function (item) {
        this.removeAll();
        this.addBreadcrumbItem(item);
    },

    addBreadcrumbItem: function (item) {
        var link = Ext.widget('breadcrumbLink', {
            text: item.data.text
        });

        var child;
        try {
            child = item.getChild();
        } catch (ex) {
            // Ignore.
        }

        if (child !== undefined && child.rendered) {
            link.setHref(item.data.href);
        } else if (child !== undefined && !child.rendered) {
            link.href = item.data.href;
        }

        this.addBreadcrumbComponent(link);

        // Recursively add the children.
        if (child !== undefined && child !== null) {
            this.addBreadcrumbItem(child);
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