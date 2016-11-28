Ext.define('Usr.controller.Workgroups', {
    extend: 'Ext.app.Controller',
    requires: [
        'Usr.store.Workgroups'
    ],
    views: [
        'Usr.view.workgroup.Setup',
        'Usr.view.workgroup.AddWorkgroup',
        'Usr.view.workgroup.AddUsersToWorkgroup'
    ],
    stores: [
        'Usr.store.Workgroups',
        'Usr.store.WorkgroupUsers'
    ],
    models: [
        'Usr.model.Workgroup',
        'Usr.model.WorkgroupUser'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'usr-workgroups-setup'
        },
        {
            ref: 'addPage',
            selector: 'usr-add-workgroup'
        },
        {
            ref: 'workgroupsGrid',
            selector: '#grd-workgroups'
        },
        {
            ref: 'workgroupPreviewContainerPanel',
            selector: '#pnl-workgroup-preview-form'
        }
    ],

    workgroupRecord: null,
    init: function () {
        this.control({
            'usr-workgroups-setup usr-workgroups-grid': {
                select: this.showPreview
            },
            'usr-workgroup-action-menu': {
                click: this.chooseAction
            },
            'usr-add-workgroup #btn-add': {
                click: this.addWorkgroup
            },
            'usr-add-workgroup #btn-cancel-link': {
                click: this.cancelAddWorkgroup
            },
            'usr-add-workgroup #btn-add-users': {
                click: this.addUsersBtn
            },
            'usr-add-users-to-workgroup #lnk-cancel-add-users': {
                click: this.cancelAddUsers
            },
            'usr-add-users-to-workgroup': {
                selecteditemsadd: this.onSelectedItemsAdd
            }
        });
    },

    showWorkgroups: function () {
        var me = this,
            view = Ext.widget('usr-workgroups-setup', {
                router: me.getController('Uni.controller.history.Router')
            });
        me.workgroupRecord = null;
        me.getApplication().fireEvent('changecontentevent', view);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('usr-workgroup-preview'),
            previewForm = page.down('usr-workgroup-preview-form'),
            usersField = previewForm.down('[name=users]'),
            usersList = [];

        Ext.suspendLayouts();
        preview.setTitle(Ext.htmlEncode(record.get('name')));
        previewForm = page.down('usr-workgroup-preview-form');
        previewForm.loadRecord(record);
        if (preview.down('usr-workgroup-action-menu')) {
            preview.down('usr-workgroup-action-menu').record = record;
        }
        record.users().each(function (user) {
            usersList.push('- ' + Ext.htmlEncode(user.get('name')));
        });
        usersField.setValue((usersList.length == 0) ? usersList = '-' : usersList.join('<br/>'));
        Ext.resumeLayouts();
    },

    chooseAction: function (menu, item) {
        var me = this,
            record = menu.record || me.getUserDirectoriesGrid().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'addUsers':
                me.addUsersBtn();
                break;
            case 'editWorkgroup':
                location.href = '#/administration/workgroups/' + record.get('id') + '/edit';
                break;
            case 'removeWorkgroup':
                me.remove(record);
                break;
        }
    },

    remove: function (record) {
        var me = this;

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.remove', 'USR', 'Remove')
        }).show({
            title: Uni.I18n.translate('workgroups.remove.title', 'USR', 'Remove \'{0}\'?', record.get('name')),
            msg: Uni.I18n.translate('workgroups.remove.message', 'USR', 'This workgroup will no longer be available.'),
            fn: function (state) {
                if (state === 'confirm') {
                    me.removeWorkgroup(record);
                }
            }
        });
    },

    removeWorkgroup: function (record) {
        var me = this,
            workgroupsGrid = me.getWorkgroupsGrid(),
            workgroupPreviewContainerPanel = me.getWorkgroupPreviewContainerPanel(),
            view = workgroupsGrid || workgroupPreviewContainerPanel;

        view.setLoading();

        record.destroy({
            success: function () {
                if (me.getPage()) {
                    workgroupsGrid.down('pagingtoolbartop').totalCount = 0;
                    workgroupsGrid.down('pagingtoolbarbottom').resetPaging();
                    workgroupsGrid.getStore().load();
                }
                else {
                    me.getController('Uni.controller.history.Router').getRoute('administration/workgroups').forward();
                }
                view.setLoading(false);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('workgroups.remove.confirmation', 'USR', 'Workgroup removed'));

            },
            failure: function (object, operation) {
                view.setLoading(false);
            }
        });
    },

    addWorkgroup: function (button) {
        var me = this,
            addPage = me.getAddPage(),
            form = addPage.down('#frm-add-workgroup'),
            workgroupRecord = addPage.workgroupRecord || Ext.create('Usr.model.Workgroup'),
            addWorkgroupForm = addPage.down('#frm-add-workgroup'),
            formErrorsPanel = addWorkgroupForm.down('#form-errors');

        if (form.isValid()) {
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }

            addWorkgroupForm.updateRecord(workgroupRecord);

            workgroupRecord.save({
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('administration/workgroups').forward();

                    if (button.action === 'edit') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('workgroups.successMsg.saved', 'USR', 'Workgroup saved'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('workgroups.successMsg.added', 'USR', 'Workgroup added'));
                    }

                    me.workgroupRecord = null;
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        addWorkgroupForm.getForm().markInvalid(json.errors);
                    }
                    formErrorsPanel.show();
                    me.workgroupRecord = null;
                }
            })
        } else {
            formErrorsPanel.show();
        }

    },

    cancelAddWorkgroup: function () {
        var me = this;

        me.workgroupRecord = null;
        me.forwardToPreviousPage();
    },

    showAddWorkgroup: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            addWorkgroupView, addWorkgroupForm;

        addWorkgroupView = Ext.create('Usr.view.workgroup.AddWorkgroup', {
            edit: false,
            returnLink: router.getRoute('administration/workgroups').buildUrl()
        });

        addWorkgroupForm = addWorkgroupView.down('#frm-add-workgroup');
        addWorkgroupForm.setTitle(Uni.I18n.translate('workgroup.add', 'USR', 'Add workgroup'));

        if (me.workgroupRecord == null) {
            me.workgroupRecord = Ext.create('Usr.model.Workgroup');
        }
        addWorkgroupView.workgroupRecord = me.workgroupRecord;
        addWorkgroupForm.loadRecord(me.workgroupRecord);
        addWorkgroupForm.down('#grd-users').reconfigure(me.workgroupRecord.users());
        addWorkgroupView.updateGrid();

        me.getApplication().fireEvent('changecontentevent', addWorkgroupView);
    },

    showEditWorkgroup: function (workgroupId) {
        var me = this;

        if (me.workgroupRecord == null) {
            var workgroup = me.getModel('Usr.model.Workgroup');
            workgroup.load(workgroupId, {
                success: function (workgroupRecord) {
                    me.fillControls(workgroupRecord);
                },
                failure: function (record, operation) {
                }
            });
        }
        else {
            me.fillControls(me.workgroupRecord);
            me.workgroupRecord = null;
        }
    },

    fillControls: function (workgroupRecord) {
        var me = this,
            router = me.getController('Uni.controller.history.Router')
        addWorkgroupView = Ext.create('Usr.view.workgroup.AddWorkgroup', {
            edit: true,
            returnLink: router.getRoute('administration/workgroups').buildUrl()
        });

        var nameField = addWorkgroupView.down('#txt-name');
        if (nameField) {
            nameField.setDisabled(true);
        }

        addWorkgroupView.workgroupRecord = workgroupRecord;
        me.getApplication().fireEvent('editWorkgroup', workgroupRecord);
        var addWorkgroupForm = addWorkgroupView.down('#frm-add-workgroup');
        addWorkgroupForm.setTitle(Ext.String.format(Uni.I18n.translate('workgroups.edit', 'USR', 'Edit \'{0}\''), workgroupRecord.get('name')));
        addWorkgroupForm.loadRecord(workgroupRecord);

        var usersStore = workgroupRecord.users();
        addWorkgroupForm.down('#grd-users').reconfigure(usersStore);
        addWorkgroupView.updateGrid();
        me.getApplication().fireEvent('changecontentevent', addWorkgroupView);
    },

    addUsersBtn: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            addPage = me.getAddPage(),
            addWorkgroupForm = addPage.down('#frm-add-workgroup');

        me.workgroupRecord = addPage.workgroupRecord;
        addWorkgroupForm.updateRecord(me.workgroupRecord);
        router.getRoute(router.currentRoute + '/users').forward();
    },

    addUsers: function () {
        var me = this;

        if (!me.workgroupRecord) {
            me.forwardToPreviousPage();
        } else {
            var me = this,
                widget = Ext.widget('usr-add-users-to-workgroup');

            me.loadAvailableUsers(widget);
            me.getApplication().fireEvent('changecontentevent', widget);
        }
    },

    forwardToPreviousPage: function () {
        var router = this.getController('Uni.controller.history.Router'),
            splittedPath = router.currentRoute.split('/');

        splittedPath.pop();
        router.getRoute(splittedPath.join('/')).forward();
    },

    loadAvailableUsers: function (view) {
        var me = this,
            availableUsers = me.getStore('Usr.store.WorkgroupUsers');

        view.setLoading();
        availableUsers.load({
            callback: function (records, operation, success) {
                var selectedUsers = me.workgroupRecord.users();
                selectedUsers.each(function (selectedUser) {
                    var rowIndex = availableUsers.findExact('id', selectedUser.get('id'));
                    if (rowIndex != -1) {
                        availableUsers.removeAt(rowIndex);
                    }
                });

                if (availableUsers.count() != 0) {
                    view.down('#grd-user-selection').reconfigure(availableUsers);
                }
                view.setLoading(false);
            }
        });
    },

    cancelAddUsers: function () {
        var me = this;
        me.forwardToPreviousPage();
    },

    onSelectedItemsAdd: function (selections) {
        var me = this;

        me.workgroupRecord.beginEdit();
        me.workgroupRecord.users().insert(0, selections);
        me.workgroupRecord.endEdit();

        me.forwardToPreviousPage();
    }
});