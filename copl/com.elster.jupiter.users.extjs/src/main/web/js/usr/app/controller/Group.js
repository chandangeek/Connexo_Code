Ext.define('Usr.controller.Group', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem',
        'Usr.controller.GroupEdit'
    ],
    stores: [
        'Usr.store.Privileges',
        'Usr.store.Groups'
    ],
    views: [
        'Usr.view.group.Browse'
    ],

    refs: [
        { ref: 'groupBrowse', selector: 'groupBrowse' },
        { ref: 'groupDetails', selector: 'groupBrowse groupDetails' }
    ],

    init: function () {
        this.control({
            'groupBrowse breadcrumbTrail': {
                afterrender: this.onAfterRender
            },
            'groupBrowse groupList': {
                selectionchange: this.selectGroup
            },
            'groupBrowse groupDetails menuitem[action=editGroup]': {
                click: this.editGroupMenu
            },
            'groupBrowse groupList actioncolumn': {
                editGroupItem: this.editGroup
            },
            'groupBrowse groupList button[action=createGroup]': {
                click: this.createGroup
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('groupBrowse');
        this.getApplication().getController('Usr.controller.Main').showContent(widget);

        Ext.StoreManager.get('Usr.store.Groups').on('load', function () {
            widget.down('#groupList').getSelectionModel().doSelect(0);
        });
    },

    onAfterRender: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.root', 'USM', 'User Management'),
            href: '#usermanagement'
        });
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('group.title', 'USM', 'Roles'),
            href: 'roles'
        });

        breadcrumbParent.setChild(breadcrumbChild);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    editGroupMenu: function (button) {
        var record = button.up('#groupBrowse').down('#groupDetailsForm').getRecord();
        this.editGroup(record);

    },

    editGroup: function (record) {
        this.getApplication().getController('Usr.controller.GroupEdit').showEditOverviewWithHistory(record.get('id'));
    },

    createGroup: function () {
        this.getApplication().getController('Usr.controller.GroupEdit').showCreateOverviewWithHistory();
    },

    selectGroup: function (grid, record) {
        if(record.length > 0){
            var panel = grid.view.up('#groupBrowse').down('#groupDetails'),
                form = panel.down('form');

            var title = Uni.I18n.translate('group.group', 'USM', 'Role') + ' "' + record[0].get('name') + '"';
            panel.setTitle(title);
            form.loadRecord(record[0]);

            var privileges = '';
            var currentPrivileges = record[0].privileges().data.items;
            for (var i = 0; i < currentPrivileges.length; i++) {
                privileges += currentPrivileges[i].data.name + '<br/>';
            }
            form.down('[name=privileges]').setValue(privileges);
            panel.show();
        }
    }
});