Ext.define('Usr.controller.Group', {
    extend: 'Ext.app.Controller',
    requires: [
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
            'groupBrowse groupList': {
                selectionchange: this.selectGroup
            },
            'groupBrowse groupDetails menuitem[action=edit]': {
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
        this.getApplication().getController('Usr.controller.Main').showContent(widget);
    },

    editGroupMenu: function (button) {
        var record = button.up('#groupBrowse').down('#groupDetailsForm').getRecord();
        this.editGroup(record);

    },

    editGroup: function (record) {
        this.getApplication().getController('Usr.controller.GroupEdit').showEditOverviewWithHistory(record.get('id'));
        this.getApplication().fireEvent('editRole', record);
    },

    createGroup: function () {
        this.getApplication().getController('Usr.controller.GroupEdit').showCreateOverviewWithHistory();
    },

    selectGroup: function (grid, record) {
        if(record.length > 0){
            var panel = grid.view.up('#groupBrowse').down('#groupDetails'),
                form = panel.down('form');

            var title = Uni.I18n.translate('group.group', 'USM', 'Role') + ' \'' + record[0].get('name') + '\'';
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