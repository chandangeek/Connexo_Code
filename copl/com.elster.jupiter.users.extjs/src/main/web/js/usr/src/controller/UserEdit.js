Ext.define('Usr.controller.UserEdit', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Usr.store.UserGroups',
        'Usr.store.Users',
        'Usr.store.UserDirectories',
        'Usr.store.Locales'
    ],

    models: [
        'Usr.model.Group',
        'Usr.model.User',
        'Usr.model.UserDirectory'
    ],

    views: [
        'Usr.view.user.Edit'
    ],

    refs: [
        {
            ref: 'selectRolesGrid',
            selector: 'userEdit #selectRoles'
        }
    ],

    init: function () {
        this.control({
            'userEdit button[action=save]': {
                click: this.updateUser
            }
        });
    },

    showEditOverviewWithHistory: function (groupId) {
        location.href = '#/administration/users/' + groupId + '/edit';
    },

    /**
     * todo: this method should be refactored.
     * @param userId
     */
    showEditOverview: function (userId) {
        var me = this,
            widget = Ext.widget('userEdit'),
            panel = widget.getCenterContainer().items.getAt(0),
            locales = this.getStore('Usr.store.Locales');

        this.backUrl = this.getApplication().getController('Usr.controller.history.UserManagement').tokenizePreviousTokens();

        this.getApplication().getController('Usr.controller.Main').showContent(widget);

        locales.load();

        widget.hide();
        widget.setLoading(true);

        Ext.ModelManager.getModel('Usr.model.User').load(userId, {
            success: function (user) {
                var title = Uni.I18n.translate('general.edit', 'USR', 'Edit'),
                    language = user.get('language');
                panel.setTitle(title + ' \'' + user.get('authenticationName') + '\'');
                if (user.get('authenticationName') == "admin") {
                    panel.down('[itemId=alertmessageuser]').show();
                }

                panel.down('[name=authenticationName]').disable();
                panel.down('[name=domain]').disable();

                Ext.StoreManager.get('Usr.store.UserGroups').load(function () {
                    panel.down('form').loadRecord(user);
                    if (language) {
                        if (locales.getCount()) {
                            panel.down('form [name=language]').setValue(language.languageTag);
                        } else {
                            locales.on('load', function () {
                                panel.down('form [name=language]').setValue(language.languageTag);
                            }, me, {single: true});
                        }
                    }

                    Ext.ModelManager.getModel('Usr.model.UserDirectory').load(user.get('domain'), {
                        callback: function (domain) {
                            if (!domain.get('manageGroupsInternal')||(user.get('authenticationName') == "admin")) {
                                panel.down('[itemId=selectRoles]').disable();
                            }

                            widget.setLoading(false);
                            widget.show();
                        }
                    });
                });
            }
        });
    },

    updateUser: function (button) {
        var me = this,
            form = button.up('form'),
            record = form.getRecord(),
            language = me.getStore('Usr.store.Locales').getById(form.down('[name=language]').getValue());

        form.updateRecord(record);
        if (language) {
            record.set('language', language.getData());
        }
        record.save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translatePlural('user.saved', record.get('authenticationName'), 'USR', 'User \'{0}\' saved.'));
                location.href = '#/administration/users';
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