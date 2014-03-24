Ext.define('Usr.controller.Group', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem'
    ],
    stores: [
        'Privileges',
        'Groups'
    ],
    models: [
        'Privilege',
        'Group'
    ],
    views: [
        'group.Browse'
    ],

    refs: [
    ],

    init: function () {
        this.initMenu();

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

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('group.title', 'USM', 'Roles'),
            href: Usr.getApplication().getHistoryGroupController().tokenizeShowOverview(),
            glyph: 'xe020@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('groupBrowse');
        //widget.getBreadcrumbTrail().show();
        this.getApplication().fireEvent('changecontentevent', widget);
        //this.getPrivilegesStore().load();
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
        this.getApplication().getGroupPrivilegesController().showOverview(record, 'edit');
    },

    createGroup: function () {
        var record = Ext.create(Usr.model.Group);
        this.getApplication().getGroupPrivilegesController().showOverview(record, 'create');
    },

    selectGroup: function (grid, record) {
        // fill in the details panel
        var form = grid.up('panel').up('container').up('container').down('form');
        form.loadRecord(record);

        var detailsHeader = Ext.getCmp('els_usm_groupDetailsHeader');
        detailsHeader.update('<h4>' + Uni.I18n.translate('group.group', 'USM', 'Role') + ' "' + record.get('name') + '"' + '</h4>');

        var privileges = '';
        var currentPrivileges = record.privileges().data.items;
        for (var i = 0; i < currentPrivileges.length; i++) {
            privileges += currentPrivileges[i].data.name + '\n';
        }
        Ext.getCmp('els_usm_groupDetailsPrivileges').setValue(privileges);
        form.show();
    }

});