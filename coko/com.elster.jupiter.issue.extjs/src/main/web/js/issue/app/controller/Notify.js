Ext.define('Isu.controller.Notify', {
    extend: 'Ext.app.Controller',

    views: [
        'workspace.issues.Notify'
    ],

    refs: [
        {
            ref: 'notifyView',
            selector: 'notify-user'
        }
    ],

    init: function () {
        this.control({
            'notify-user breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'notify-user button[action=notify]': {
                click: this.onClick
            }
        });
    },

    showOverview: function () {
        var self = this,
            widget = Ext.widget('notify-user');
        self.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Workspace',
                href: '#/workspace'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issues',
                href: 'issues'
            }),
            breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Notify user',
                href: 'notify'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    onClick: function () {
        var notifyView = this.getNotifyView(),
            form = notifyView.down('form').getForm();
        if (form.isValid()) {
            alert('Hooray!');
        }
    }

});



