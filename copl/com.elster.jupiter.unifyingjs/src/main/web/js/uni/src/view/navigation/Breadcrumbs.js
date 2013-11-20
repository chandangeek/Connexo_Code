Ext.define('Uni.view.navigation.Breadcrumbs', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationBreadcrumbs',

    requires: [
        'Uni.view.navigation.breadcrumb.Link',
        'Uni.view.navigation.breadcrumb.Separator'
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
        var link = Ext.widget('navigationBreadcrumbLink', {
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
        console.log(itemCount);
        if (itemCount % 2 === 1) {
            this.add(Ext.widget('navigationBreadcrumbSeparator'));
        }

        this.add(component);
    }
});