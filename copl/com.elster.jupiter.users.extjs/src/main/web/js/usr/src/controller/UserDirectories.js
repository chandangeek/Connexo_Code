Ext.define('Usr.controller.UserDirectories', {
    extend: 'Ext.app.Controller',
    requires: [
        'Usr.store.MgmUserDirectories'
    ],
    views: [
        'Usr.view.userDirectory.Setup',
        'Usr.view.userDirectory.AddUserDirectory'/*,
         'Usr.view.userDirectory.Details',
         'Usr.view.userDirectory.Menu'*/
    ],
    stores: [
        'Usr.store.MgmUserDirectories',
        'Usr.store.SecurityProtocols'
    ],
    models: [
        'Usr.model.MgmUserDirectory',
         'Usr.model.MgmUserDirectory'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'usr-user-directories-setup'
        },
        {
            ref: 'addPage',
            selector: 'usr-add-user-directory'
        }, /*,
         {
         ref: 'detailsImportService',
         selector: 'fin-details-import-service'
         },*/
        {
            ref: 'userDirectoriesGrid',
            selector: '#grd-user-directories'
        },
        {
            ref: 'userDirectoryPreviewContainerPanel',
            selector: '#pnl-user-directory-preview-form'
        }/*,
         {
         ref: 'userDirectoryOverview',
         selector: '#frm-import-service-details'
         }*/
    ],

    init: function () {
        this.control({
            'usr-user-directories-setup usr-user-directories-grid': {
                select: this.showPreview
            },
            'usr-user-directory-action-menu': {
                click: this.chooseAction,
                show: this.onShowUserDirectoryMenu
            },
            'usr-add-user-directory #btn-add': {
                click: this.addUserDirectory
            }
        });
    },

    showUserDirectories: function () {
        var me = this,
            view = Ext.widget('usr-user-directories-setup', {
                router: me.getController('Uni.controller.history.Router')
            });
        me.getApplication().fireEvent('changecontentevent', view);

    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('usr-user-directory-preview'),
            previewForm = page.down('usr-user-directory-preview-form');

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        preview.down('usr-user-directory-action-menu').record = record;
        Ext.resumeLayouts();
    },

    chooseAction: function (menu, item) {
        var me = this,
            record = menu.record || me.getUserDirectoriesGrid().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'synchronizeUserDirectory':
                location.href = '#/administration/userdirectories/' + record.get('id') + '/synchronize';
                break;
            case 'editUserDirectory':
                location.href = '#/administration/userdirectories/' + record.get('id') + '/edit';
                break;
            case 'removeUserDirectory':
                me.remove(record);
                break;
            case 'setAsDefault':
                me.setAsDefault(record);
                break;
        }
    },

    onShowUserDirectoryMenu: function(menu){
        var me = this,
            editUserDirectory = menu.down('#edit-user-directory'),
            synchronizeUserDirectory = menu.down('#synchronize-user-directory'),
            removeUserDirectory = menu.down('#remove-user-directory'),
            setAsDefault = menu.down('#set-as-default-user-directory'),
            isEditUserDirectory = true, isSynchronizeUserDirectory = true, isRemoveUserDirectory = true, isSetAsDefault = true;

        if (menu.record.get('isDefault')) {
            isSetAsDefault = false;
            isRemoveUserDirectory = false;
        }

        if (menu.record.get('name') === 'Local') {
            isEditUserDirectory = false;
            isSynchronizeUserDirectory = false;
            isRemoveUserDirectory = false;
        }

        editUserDirectory && editUserDirectory.setVisible(isEditUserDirectory);
        synchronizeUserDirectory && synchronizeUserDirectory.setVisible(isSynchronizeUserDirectory);
        removeUserDirectory && removeUserDirectory.setVisible(isRemoveUserDirectory);
        setAsDefault && setAsDefault.setVisible(isSetAsDefault);
    },

    remove: function (record) {
        var me = this;

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.remove', 'USR', 'Remove')
        }).show({
            title: Ext.String.format(Uni.I18n.translate('userDirectories.remove.title', 'FIM', 'Remove \'{0}\'?'), record.get('name')),
            msg: Uni.I18n.translate('userDirectories.remove.message', 'FIM', 'This user directory will no longer be available.'),
            fn: function (state) {
                if (state === 'confirm') {
                    me.removeUserDirectory(record);
                }
            }
        });
    },

    removeUserDirectory: function (record) {
        var me = this,
            userDirectoriesGrid = me.getUserDirectoriesGrid(),
            userDirectoryPreviewContainerPanel = me.getUserDirectoryPreviewContainerPanel(),
            view = userDirectoriesGrid || userDirectoryPreviewContainerPanel;

        view.setLoading();

        record.destroy({
            success: function () {
                if (me.getPage()) {
                    userDirectoriesGrid.down('pagingtoolbartop').totalCount = 0;
                    userDirectoriesGrid.down('pagingtoolbarbottom').resetPaging();
                    userDirectoriesGrid.getStore().load();
                } else {
                    me.getController('Uni.controller.history.Router').getRoute('administration/userdirectories').forward();
                }
                view.setLoading(false);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('userDirectories.remove.confirmation', 'USR', 'User directory removed'));

            },
            failure: function (object, operation) {
                view.setLoading(false);
            }
        });
    },

    setAsDefault: function (record){
        var me = this;

        record.set('isDefault', true);

        record.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('userDirectories.acknowlegment.setAsDefault', 'USR', 'User directory set as default'));
            },
            callback: function () {
                me.getUserDirectoriesGrid().store.load();
            }
        });
    },

    addUserDirectory: function (button) {
        var me = this,
            addPage = me.getAddPage(),
            userDirectoryRecord = addPage.userDirectoryRecord || Ext.create('Usr.model.MgmUserDirectory'),
            addUserDirectoryForm = addPage.down('#frm-add-user-directory'),
            formErrorsPanel = addUserDirectoryForm.down('#form-errors');

        if (!addUserDirectoryForm.isValid()) {
            formErrorsPanel.show();
        } else {
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }

            addUserDirectoryForm.updateRecord(userDirectoryRecord);
            userDirectoryRecord.beginEdit();
            userDirectoryRecord.set('securityProtocolInfo', {
                name: addUserDirectoryForm.down('#cbo-security-protocol').getValue()
            });
            userDirectoryRecord.endEdit();

            userDirectoryRecord.save({
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('administration/userdirectories').buildUrl();

                    if (button.action === 'edit') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('userDirectories.successMsg.saved', 'USR', 'User directory saved'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('userDirectories.successMsg.added', 'USR', 'User directory added'));
                    }
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        addUserDirectoryForm.getForm().markInvalid(json.errors);
                    }
                    formErrorsPanel.show();
                }
            })
        }
    },

    showAddUserDirectory: function () {
        var me = this,
            userDirectoriesGrid = me.getUserDirectoriesGrid(),
            userDirectoryPreviewContainerPanel = me.getUserDirectoryPreviewContainerPanel(),
            router = me.getController('Uni.controller.history.Router'),
            addUserDirectoryView, addUserDirectoryForm;

        addUserDirectoryView = Ext.create('Usr.view.userDirectory.AddUserDirectory', {
            edit: false,
            returnLink: router.getRoute('administration/userdirectories').buildUrl()
        });

        var securityProtocolCombo = addUserDirectoryView.down('#cbo-security-protocol');
        if (securityProtocolCombo.store.getCount() == 0) {
            securityProtocolCombo.allowBlank = true;
            securityProtocolCombo.down('#no-security-protocol').show();
        }

        addUserDirectoryForm = addUserDirectoryView.down('#frm-add-user-directory');
        addUserDirectoryForm.setTitle(Uni.I18n.translate('userDirectories.add', 'USR', 'Add user directory'));

        me.getApplication().fireEvent('changecontentevent', addUserDirectoryView);
    },

    showEditImportService: function (userDirectoryId) {
        var me = this,
            userDirectoriesGrid = me.getUserDirectoriesGrid(),
            userDirectoryPreviewContainerPanel = me.getUserDirectoryPreviewContainerPanel(),
            router = me.getController('Uni.controller.history.Router'),
            addUserDirectoryView, addUserDirectoryForm;

        addUserDirectoryView = Ext.create('Usr.view.userDirectory.AddUserDirectory', {
            edit: true,
            returnLink: router.getRoute('administration/userdirectories').buildUrl()
        });

        var securityProtocolCombo = addUserDirectoryView.down('#cbo-security-protocol');
        if (securityProtocolCombo.store.getCount() == 0) {
            securityProtocolCombo.allowBlank = true;
            securityProtocolCombo.down('#no-security-protocol').show();
        }

        var userDirectory = me.getModel('Usr.model.MgmUserDirectory');
        userDirectory.load(userDirectoryId, {
            success: function (userDirectoryRecord) {
                addUserDirectoryView.userDirectoryRecord = userDirectoryRecord;
                me.getApplication().fireEvent('editUserDirectory', userDirectoryRecord);

                addUserDirectoryForm = addUserDirectoryView.down('#frm-add-user-directory');
                addUserDirectoryForm.setTitle(Ext.String.format(Uni.I18n.translate('userDirectories.edit', 'USR', 'Edit \'{0}\''), userDirectoryRecord.get('name')));

                addUserDirectoryForm.loadRecord(userDirectoryRecord);
                me.getApplication().fireEvent('changecontentevent', addUserDirectoryView);
            }
        });
    },

    synchronizeUserDirectory: function () {
        alert('synchronizeUserDirectory');
    }

});