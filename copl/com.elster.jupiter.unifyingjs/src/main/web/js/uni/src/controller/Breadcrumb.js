Ext.define('Uni.controller.Breadcrumb', {
    extend: 'Ext.app.Controller',

    views: [
        'breadcrumb.Trail'
    ],

    refs: [
        {
            ref: 'breadcrumbTrail',
            selector: 'breadcrumbTrail'
        }
    ],

    init: function () {
        this.getApplication().on('setbreadcrumbitemevent', this.setBreadcrumbItem, this);
        this.getApplication().on('addbreadcrumbitemevent', this.addBreadcrumbItem, this);
        this.getApplication().on('addbreadcrumbcomponentevent', this.addBreadcrumbComponent, this);
    },

    setBreadcrumbItem: function (breadcrumbItem) {
        this.getBreadcrumbTrail().setBreadcrumbItem(breadcrumbItem);
    },

    addBreadcrumbItem: function (breadcrumbItem) {
        this.getBreadcrumbTrail().addBreadcrumbItem(breadcrumbItem);
    },

    addBreadcrumbComponent: function (breadcrumbComponent) {
        this.getBreadcrumbTrail().addBreadcrumbComponent(breadcrumbComponent);
    }
});