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
    views: [
        'Usr.view.group.Browse'
    ],

    init: function () {
        this.control({
            'groupBrowse breadcrumbTrail': {
                afterrender: this.onAfterRender
            },
            '#groupList': {
                selectionchange: this.selectGroup
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
        this.getApplication().getController('Usr.controller.Main').showContent(widget);
        widget.setLoading(true);

        var me = this;
        Ext.StoreManager.get('Usr.store.Groups').on('load', function onLoad () {
            widget.setLoading(false);
            if(this.data.items.length > 0){
                widget.show();
                widget.down('#groupList').getSelectionModel().doSelect(0);
            }
        });
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
        var record = button.up('#groupBrowse').down('#groupDetailsForm').getRecord();
        this.editGroup(record);

    },

    editGroup: function (record) {
        this.getApplication().getController('Usr.controller.GroupPrivileges').showEditOverviewWithHistory(record.get('id'));
    },

    createGroup: function () {
        this.getApplication().getController('Usr.controller.GroupPrivileges').showCreateOverviewWithHistory();
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