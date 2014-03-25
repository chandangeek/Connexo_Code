Ext.define('Usr.controller.Home', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem'
    ],
    views: [
        'Home'
    ],

    init: function () {
        this.control({
            /*'Home breadcrumbTrail': {
                afterrender: this.onAfterRender
            }*/
        })
    },
    /*onAfterRender: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.root', 'USM', 'User Management'),
            href: '#'
        });

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },*/
    showOverview: function () {
        var widget = Ext.widget('Home');
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});