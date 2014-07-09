Ext.define('Usr.controller.GroupEdit', {
    extend: 'Ext.app.Controller',
    requires: [
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
        'Usr.view.group.Edit'
    ],
    refs: [
        {
            ref: 'selectPrivilegesGrid',
            selector: 'groupEdit #selectPrivileges'
        }
    ],

    init: function () {
        this.control({
            'groupEdit button[action=save]': {
                click: this.saveGroup
            },
            'groupEdit button[action=cancel]': {
                click: this.back
            }
        });
    },

    backUrl: null,

    back: function () {
        location.href = this.backUrl;
    },

    showEditOverviewWithHistory: function(groupId) {
        location.href = '#usermanagement/roles/' + groupId + '/edit';
    },

    showEditOverview: function (groupId) {
        var me = this;
        me.mode = 'edit';
        Ext.ModelManager.getModel('Usr.model.Group').load(groupId, {
            success: function (group) {
                me.showOverview(group, Uni.I18n.translate('group.edit', 'USM', 'Edit') + ' \'' + group.get('name') + '\'');
            }
        });
    },

    showCreateOverviewWithHistory: function (groupId) {
        location.href = '#usermanagement/roles/add';
    },

    showCreateOverview: function () {
        this.mode = 'create';
        this.showOverview(Ext.create('Usr.model.Group'), Uni.I18n.translate('group.create', 'USM', 'Add role'));
    },

    showOverview: function (record, title) {
        var me = this,
            widget = Ext.widget('groupEdit', {edit: (me.mode == 'edit')}),
            panel = widget.getCenterContainer().items.getAt(0);

        this.backUrl = this.getApplication().getController('Usr.controller.history.UserManagement').tokenizePreviousTokens();

        widget.setLoading(true);
        panel.setTitle(title);

        me.getStore('Usr.store.Privileges').load(function () {
            widget.down('form').loadRecord(record);

            widget.setLoading(false);

            me.getApplication().getController('Usr.controller.Main').showContent(widget);
        });
    },

    saveGroup: function (button) {
        var me = this;
        var form = button.up('form');

        form.updateRecord();
        form.getRecord().save({
            success: function (record) {
                var message;
                if(me.mode == 'edit'){
                    message = Uni.I18n.translatePlural('group.saved', record.get('name'), 'USM', 'Role \'{0}\' saved.');
                }
                else{
                    message = Uni.I18n.translatePlural('group.added', record.get('name'), 'USM', 'Role \'{0}\' added.');
                }
                me.getApplication().fireEvent('acknowledge', message);
                me.back();
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.markInvalid(json.errors);
                }
            }
        });
    }
});