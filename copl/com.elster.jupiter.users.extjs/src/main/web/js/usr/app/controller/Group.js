Ext.define('Usr.controller.Group', {
    extend: 'Ext.app.Controller',
    requires: [
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
            selector: 'groupBrowse groupDetails'
        }
    ],

    init: function () {
        this.control({
            'groupBrowse groupList': {
                itemclick: this.selectGroup
            },
            'groupBrowse groupDetails menuitem[action=editGroup]': {
                click: this.editGroupMenu
            },
            'groupBrowse groupList uni-actioncolumn': {
                edit: this.editGroup
            },
            'groupBrowse groupList button[action=createGroup]': {
                click: this.createGroup
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('groupBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
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
        var form = this.getGroupDetails();

        var title = Uni.I18n.translate('group.group', 'USM', 'Role') + ' "' + record.get('name') + '"';
        form.setTitle(title);
        form.loadRecord(record);

        var privileges = '';
        var currentPrivileges = record.privileges().data.items;
        for (var i = 0; i < currentPrivileges.length; i++) {
            privileges += currentPrivileges[i].data.name + '<br/>';
        }
        form.down('[name=privileges]').setValue(privileges);
        form.show();
    }
});