Ext.define('Usr.controller.GroupPrivileges', {
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

    showEditOverviewWithHistory: function (groupId) {
        location.href = '#/roles/' + groupId + '/edit';
    },

    /**
     * todo: merge edit and create methods to reduce code duplicateness.
     * @param groupId
     */
    showEditOverview: function (groupId) {
        var me = this;
        var groupStore = this.getStore('Usr.store.Groups');
        var widget = Ext.widget('groupEdit');
        var panel = widget.getCenterContainer().items.getAt(0);

        this.backUrl = '#/roles/' + groupId;

        widget.hide();
        widget.setLoading(true);

        Ext.ModelManager.getModel('Usr.model.Group').load(groupId, {
            success: function (group) {
                groupStore.load({
                    callback: function (store) {
                        var title = Uni.I18n.translate('group.edit', 'USM', 'Edit role');
                        panel.setTitle(title + ' "' + group.get('name') + '"');

                        widget.down('[name=name]').disable();

                        me.getStore('Usr.store.Privileges').load(function () {
                            widget.down('form').loadRecord(group);

                            me.getApplication().fireEvent('changecontentevent', widget);

                            widget.setLoading(false);
                            widget.show();
                        });
                    }
                })
            }
        });
    },

    showCreateOverviewWithHistory: function (groupId) {
        location.href = '#roles/create';
    },

    showCreateOverview: function () {
        var me = this;
        var record = Ext.create('Usr.model.Group');
        var widget = Ext.widget('groupEdit');
        var panel = widget.getCenterContainer().items.getAt(0);
        var title = Uni.I18n.translate('group.create', 'USM', 'Create role');

        this.backUrl = '#/roles';

        widget.setLoading(true);
        panel.setTitle(title);

        me.getStore('Usr.store.Privileges').load(function () {
            widget.down('form').loadRecord(record);

            widget.setLoading(false);

            me.getApplication().fireEvent('changecontentevent', widget);
        });
    },

    saveGroup: function (button) {
        var me = this;
        var form = button.up('form');

        form.updateRecord();
        form.getRecord().save({
            success: function (record) {
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