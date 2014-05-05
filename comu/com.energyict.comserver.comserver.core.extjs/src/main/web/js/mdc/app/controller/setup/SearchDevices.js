Ext.define('Mdc.controller.setup.SearchDevices', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification'
    ],

    views: [
        'setup.SearchDevices'
    ],

    stores: [
        //'Devices'
    ],

    refs: [
        //{ref: 'registerTypeGrid', selector: '#registertypegrid'}
    ],

    init: function () {
        this.control({
            '#searchDevices breadcrumbTrail': {
                afterrender: this.showBreadCrumb
            }
        });
    },

    showSearchDevices : function () {
        var widget = Ext.widget('searchDevices');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    showBreadCrumb: function (breadcrumbs) {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('searchDevices.searchDevices', 'MDC', 'Search devices'),
            href: 'searchdevices'
        });

        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    }
});