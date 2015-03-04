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
                select: this.selectGroup
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
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    editGroupMenu: function (button) {
        var record = button.up('#groupBrowse').down('#groupDetailsForm').getRecord();
        this.editGroup(record);
    },

    editGroup: function (record) {
        this.getApplication().getController('Usr.controller.GroupEdit').showEditOverviewWithHistory(record.get('id'));
        this.getApplication().fireEvent('editRole', record);
    },

    selectGroup: function (selectionModel, record) {
        var me = this,
            page = me.getGroupBrowse(),
            form = page.down('#groupDetailsForm');

        page.down('groupDetails').setTitle(record.get('name'));
        form.loadRecord(record);
    }
});