Ext.define('Usr.controller.GroupPrivileges', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem'
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
        },
        {
            ref: 'breadCrumbs',
            selector: 'breadcrumbTrail'
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

    back: function() {
        location.href = this.backUrl;
    },

    showEditOverviewWithHistory: function(groupId) {
        location.href = '#roles/' + groupId + '/edit';
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

        this.backUrl = this.getApplication().getController('Usr.controller.history.Group').tokenizePreviousTokens();

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
                            me.displayBreadcrumb(title + ' "' + group.get("name") + '"');

                            widget.setLoading(false);
                            widget.show();
                        });
                    }
                })
            }
        });
    },

    showCreateOverviewWithHistory: function(groupId) {
        location.href = '#roles/create';
    },

    showCreateOverview: function () {
        var me = this;
        var record = Ext.create('Usr.model.Group');
        var widget = Ext.widget('groupEdit');
        var panel = widget.getCenterContainer().items.getAt(0);
        var title = Uni.I18n.translate('group.create', 'USM', 'Create role');

        this.backUrl = this.getApplication().getController('Usr.controller.history.Group').tokenizePreviousTokens();

        widget.setLoading(true);
        panel.setTitle(title);

        me.getStore('Usr.store.Privileges').load(function () {
            widget.down('form').loadRecord(record);

            widget.setLoading(false);

            me.getApplication().fireEvent('changecontentevent', widget);
            me.displayBreadcrumb(title);
        });
    },

    displayBreadcrumb: function (current) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.root', 'USM', 'User Management'),
            href: '#'
        });
        var breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('group.title', 'USM', 'Roles'),
            href: 'roles'
        });

        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: current
        });

        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    saveGroup: function (button) {
        var me = this;
        var form = button.up('form');

        form.updateRecord();
        form.getRecord().save({
            success: function (record) {
                me.back();
            },
            failure: function(record,operation){
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.markInvalid(json.errors);
                }
            }
        });
    }
});