Ext.define('Usr.controller.Group', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem',
        'Usr.controller.GroupPrivileges'
    ],
    stores: [
        'Usr.store.Privileges',
        'Usr.store.Groups'
    ],
    models: [
        'Usr.model.Privilege',
        'Usr.model.Group'
    ],
    views: [
        'Usr.view.group.Browse'
    ],

    refs: [
        {
            ref: 'groupDetails',
            selector: 'groupDetails'
        }
    ],

    init: function () {
        this.control({
            'groupBrowse breadcrumbTrail': {
                afterrender: this.onAfterRender
            },
            '#groupList': {
                itemclick: this.selectGroup
            },
            '#groupDetails menuitem[action=editGroup]': {
                click: this.editGroupMenu
            },
            '#groupList actioncolumn': {
                editGroupItem: this.editGroup
            },
            '#groupList button[action=createGroup]': {
                click: this.createGroup
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('groupBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onAfterRender: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.root', 'USM', 'User Management'),
            href: '#'
        });
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('group.title', 'USM', 'Roles'),
            href: 'roles'
        });

        breadcrumbParent.setChild(breadcrumbChild);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    editGroupMenu: function (button) {
        var record = button.up('form').up('form').getRecord();
        this.editGroup(record);

    },

    editGroup: function (record) {
        this.getApplication().getController('Usr.controller.GroupPrivileges').showEditOverviewWithHistory(record.get('id'));
    },

    createGroup: function () {
        this.getApplication().getController('Usr.controller.GroupPrivileges').showCreateOverviewWithHistory();
    },

    selectGroup: function (grid, record) {
        // fill in the details panel
        var panel = this.getGroupDetails(),
            form = panel.down('form');

        var title = Uni.I18n.translate('group.group', 'USM', 'Role') + ' "' + record.get('name') + '"';
        panel.setTitle(title);
        form.loadRecord(record);

        var privileges = '';
        var currentPrivileges = record.privileges().data.items;
        for (var i = 0; i < currentPrivileges.length; i++) {
            privileges += currentPrivileges[i].data.name + '<br/>';
        }
        form.down('[name=privileges]').setValue(privileges);
        panel.show();
    }
});