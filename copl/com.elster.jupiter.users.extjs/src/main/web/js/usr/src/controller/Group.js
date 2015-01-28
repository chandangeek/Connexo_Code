Ext.define('Usr.controller.Group', {
    extend: 'Ext.app.Controller',
    requires: [
        'Usr.controller.GroupEdit'
    ],
    stores: [
        'Usr.store.Resources',
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

    selectGroup: function (grid, record) {
        if (record.length > 0) {
            var panel = grid.view.up('#groupBrowse').down('#groupDetails'),
                form = panel.down('form');

            panel.setTitle(record[0].get('name'));
            form.loadRecord(record[0]);

            panel.show();
        }
    }
});