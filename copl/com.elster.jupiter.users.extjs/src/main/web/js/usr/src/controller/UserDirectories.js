/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.controller.UserDirectories', {
    extend: 'Ext.app.Controller',
    requires: [
        'Usr.store.MgmUserDirectories',
        'Usr.view.userDirectory.AddUsersGrid',
		'Usr.view.userDirectory.AddGroupsGrid'
    ],
    views: [
        'Usr.view.userDirectory.Setup',
        'Usr.view.userDirectory.AddUserDirectory',
        'Usr.view.userDirectory.Synchronize',
        'Usr.view.userDirectory.AddUsersSetup',
		'Usr.view.userDirectory.AddGroupsSetup'
    ],
    stores: [
        'Usr.store.MgmUserDirectories',
        'Usr.store.SecurityProtocols',
        'Usr.store.MgmUserDirectoryUsers',
        'Usr.store.MgmUserDirectoryExtUsers',
        'Usr.store.Certificates',
        'Usr.store.TrustStores',
		'Usr.store.MgmUserDirectoryGroups',
		'Usr.store.MgmUserDirectoryExtGroups'
    ],
    models: [
        'Usr.model.MgmUserDirectory',
        'Usr.model.MgmUserDirectory',
        'Usr.model.MgmUserDirectoryUser',
        'Usr.model.MgmUserDirectoryUsers',
		'Usr.model.MgmUserDirectoryGroup',
		'Usr.model.MgmUserDirectoryGroups'
    ],
    refs: [
        {
            ref: 'page',
            selector: 'usr-user-directories-setup'
        },
        {
            ref: 'addPage',
            selector: 'usr-add-user-directory'
        },
        {
            ref: 'userDirectoriesGrid',
            selector: '#grd-user-directories'
        },
        {
            ref: 'userDirectoriyUsersGrid',
            selector: '#grd-user-directory-users'
        },
        {
            ref: 'userDirectoryPreviewContainerPanel',
            selector: '#pnl-user-directory-preview-form'
        },
        {
            ref: 'addExtUsersGrid',
            selector: '#pnl-select-users #grd-add-ext-users'
        },
        {
            ref: 'addExtGroupsGrid',
            selector: '#pnl-select-groups #grd-add-ext-groups'
        }
    ],
    localDomainName: 'Local',
    userDirectoryUsersStoreLoaded: false,
	userDirectoryGroupsStoreLoaded: false,
    init: function () {
        this.control({
            'usr-user-directories-setup usr-user-directories-grid': {
                select: this.showPreview
            },
            'usr-user-directory-action-menu': {
                click: this.chooseAction
            },
            'usr-add-user-directory #btn-add': {
                click: this.addUserDirectory
            },
            '#frm-user-directory-users #btn-save-user': {
                click: this.saveUsers
            },
            'usr-add-users-grid #btn-add-ext-user': {
                click: this.addExtUsers
            },
			'usr-add-groups-grid #btn-add-ext-group': {
                click: this.addExtGroups
			},
            'usr-add-user-directory': {
                displayinfo: this.displayInfo,
				displayextendedinfo: this.displayExtendedInfo
            },
            '#btn-user-directory-synchronize-users': {
                click: this.synchronizeUsers
            },
            '#btn-user-directory-add-users': {
                click: this.selectUsers
            },
			'#btn-user-directory-add-groups': {
                click: this.selectGroups
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
        preview.setTitle(Ext.htmlEncode(record.get('name')));
        previewForm.down('#ctn-user-directory-properties1').setVisible(record.get('name') !== me.localDomainName);
        previewForm.down('#ctn-user-directory-properties2').setVisible(record.get('name') !== me.localDomainName);
        previewForm.loadRecord(record);
        previewForm.down('#protocol-source-certificates').setVisible(record.get('certificateAlias'));
        previewForm.down('#protocol-source-trustStores').setVisible(record.get('trustStore'));
		previewForm.down('#usr-user-directory-user-base-dn').setVisible(record.get('baseUser') || !record.get('groupName'));
		previewForm.down('#usr-user-directory-user-group-dn').setVisible(record.get('groupName'));
        preview.down('usr-user-directory-action-menu').record = record;
        preview.down('#btn-user-directory-preview-action-menu').setVisible(!(record.get('id') === 0 && record.get('isDefault')));
        Ext.resumeLayouts();
    },

    chooseAction: function (menu, item) {
        var me = this,
            record = menu.record || me.getUserDirectoriesGrid().getSelectionModel().getLastSelected();

        switch (item.action) {
            case 'synchronizeUserDirectory':
                me.userDirectoryUsersStoreLoaded = false;
				me.userDirectoryGroupsStoreLoaded = false;
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

    remove: function (record) {
        var me = this;

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.remove', 'USR', 'Remove')
        }).show({
            title: Uni.I18n.translate('userDirectories.remove.title', 'USR', 'Remove \'{0}\'?', record.get('name')),
            msg: Uni.I18n.translate('userDirectories.remove.message', 'USR', 'This user directory will no longer be available.'),
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
                }
                else {
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

    setAsDefault: function (record) {
        var me = this;

        record.set('isDefault', true);
        record.save({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('userDirectories.acknowledgment.setAsDefault', 'USR', 'User directory set as default'));
            },
            callback: function () {
                me.getUserDirectoriesGrid().store.load();
            }
        });
    },

    addUserDirectory: function (button) {
        var me = this,
            addPage = me.getAddPage(),
            form = addPage.down('#frm-add-user-directory'),
            userDirectoryRecord = addPage.userDirectoryRecord || Ext.create('Usr.model.MgmUserDirectory'),
            addUserDirectoryForm = addPage.down('#frm-add-user-directory'),
            formErrorsPanel = addUserDirectoryForm.down('#form-errors');

        if (form.isValid()) {
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }

            addUserDirectoryForm.updateRecord(userDirectoryRecord);
            userDirectoryRecord.beginEdit();
            if(addPage.down('#cbo-security-protocol').getValue() !== 'NONE'){
                userDirectoryRecord.set('trustStore', {id: addPage.down('#cbo-trust-store').getValue()});
                if (addPage.down('#authentication-checkbox').getValue()) {
                    userDirectoryRecord.set('certificateAlias', addPage.down('#cbo-certificate-alias').getRawValue());
                } else {
                    userDirectoryRecord.set('certificateAlias', null);
                }
            }
			
			var dnTypeValue = userDirectoryRecord.get('dnType');
			if (dnTypeValue === 'GDN') {
				userDirectoryRecord.set('baseUser', null);
			} else if(dnTypeValue === 'UDN') {
				userDirectoryRecord.set('groupName', null);
			}

            userDirectoryRecord.set('securityProtocolInfo', {
                name: addUserDirectoryForm.down('#cbo-security-protocol').getValue()
            });
            userDirectoryRecord.endEdit();

            userDirectoryRecord.save({
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('administration/userdirectories').forward();

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
        } else {
            formErrorsPanel.show();
        }

    },

    showAddUserDirectory: function () {
        var me = this,
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

        var nameField = addUserDirectoryView.down('#txt-name');
        if (nameField) {
            nameField.setDisabled(true);
        }

        var typeField = addUserDirectoryView.down('#rdo-user-directory-type');
        if (typeField) {
            typeField.setDisabled(true);
        }
		
		var dnTypeRadioGroup = addUserDirectoryView.down('#rdo-user-dn-type');
		if (dnTypeRadioGroup) {
			dnTypeRadioGroup.setDisabled(true);
		}

        var userDirectory = me.getModel('Usr.model.MgmUserDirectory');
        userDirectory.load(userDirectoryId, {
            success: function (userDirectoryRecord) {
                addUserDirectoryView.userDirectoryRecord = userDirectoryRecord;
                me.getApplication().fireEvent('editUserDirectory', userDirectoryRecord);

                addUserDirectoryForm = addUserDirectoryView.down('#frm-add-user-directory');
                addUserDirectoryForm.setTitle(Ext.String.format(Uni.I18n.translate('userDirectories.edit', 'USR', 'Edit \'{0}\''), userDirectoryRecord.get('name')));

                addUserDirectoryView.loadRecord(userDirectoryRecord);
                me.getApplication().fireEvent('changecontentevent', addUserDirectoryView);
                addUserDirectoryView.setLoading(false);
            },
            failure: function (record, operation) {
            }
        });
        addUserDirectoryView.setLoading();
    },

    showSynchronizeUserDirectory: function (userDirectoryId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usersView, usersForm, addUserButton, userDirectoryUsersStore, addGroupButton, userDirectoryGroupsStore;

        usersView = Ext.create('Usr.view.userDirectory.Synchronize', {});
        usersView.setLoading();

        userDirectoryUsersStore = me.getStore('Usr.store.MgmUserDirectoryUsers');
		userDirectoryGroupsStore = me.getStore('Usr.store.MgmUserDirectoryGroups');
        usersForm = usersView.down('#frm-user-directory-users');
		groupsPanel = usersView.down('#panel-user-directory-groups');

        var userDirectory = me.getModel('Usr.model.MgmUserDirectory');
        userDirectory.load(userDirectoryId, {
            success: function (userDirectoryRecord) {
				var baseGroupValue = userDirectoryRecord.get('baseGroup');
				var groupPanelVisible = baseGroupValue && baseGroupValue.trim() !== '';
				groupsPanel.setVisible(groupPanelVisible);
                usersForm.setTitle(Ext.String.format(Uni.I18n.translate('userDirectories.editSynchronize', 'USR', 'Synchronize \'{0}\''), userDirectoryRecord.get('name')));
                addUserButton = usersForm.down('#btn-user-directory-add-users');
                addUserButton.href = router.getRoute('administration/userdirectories/synchronize/addUsers').buildUrl({userDirectoryId: userDirectoryId})
				if(groupPanelVisible) {
					addGroupButton = usersForm.down('#btn-user-directory-add-groups');
					addGroupButton.href = router.getRoute('administration/userdirectories/synchronize/addGroups').buildUrl({userDirectoryId: userDirectoryId})
				}
                me.getApplication().fireEvent('synchronizeUserDirectory', userDirectoryRecord);
                me.getApplication().fireEvent('changecontentevent', usersView);
				
				if (!me.userDirectoryUsersStoreLoaded) {
					userDirectoryUsersStore.loadData([], false);
					userDirectoryUsersStore.getProxy().setUrl(userDirectoryId);
					userDirectoryUsersStore.load({
						scope: this,
						callback: function (records, operation, success) {
							me.userDirectoryUsersStoreLoaded = true;
							if(groupPanelVisible && !me.userDirectoryGroupsStoreLoaded) {
								userDirectoryGroupsStore.loadData([], false);
								userDirectoryGroupsStore.getProxy().setUrl(userDirectoryId);
								userDirectoryGroupsStore.load({
									scope: this,
									callback: function (records, operation, success) {
										me.userDirectoryGroupsStoreLoaded = true;
										usersView.setLoading(false);
									}
								});
							} else {
								usersView.setLoading(false);
							}
						}
					});
				}
				else if(groupPanelVisible && !me.userDirectoryGroupsStoreLoaded) {
					userDirectoryGroupsStore.loadData([], false);
					userDirectoryGroupsStore.getProxy().setUrl(userDirectoryId);
					userDirectoryGroupsStore.load({
						scope: this,
						callback: function (records, operation, success) {
							me.userDirectoryGroupsStoreLoaded = true;
							usersView.setLoading(false);
						}
					});
				}
                else {
                    usersView.setLoading(false);
                }
            }
        });

    },

    showSelectUsers: function (userDirectoryId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            allUsersForm, cancelAddExtUserButton, addExtUsersGrid,
            userDirectoryExtUsersStore = me.getStore('Usr.store.MgmUserDirectoryExtUsers'),
            userDirectoryUsersStore = me.getStore('Usr.store.MgmUserDirectoryUsers'),
            allUsersView = Ext.create('Usr.view.userDirectory.AddUsersSetup', {userDirectoryId: userDirectoryId});

        cancelAddExtUserButton = allUsersView.down('#grd-add-ext-users #btn-cancel-add-ext-users');
        cancelAddExtUserButton.href = router.getRoute('administration/userdirectories/synchronize').buildUrl({userDirectoryId: userDirectoryId})

        userDirectoryExtUsersStore.loadData([], false);
        me.getApplication().fireEvent('changecontentevent', allUsersView);
        addExtUsersGrid = me.getAddExtUsersGrid();

        userDirectoryExtUsersStore.getProxy().setUrl(userDirectoryId);
        userDirectoryExtUsersStore.load({
            callback: function (records, operation, success) {

                if (router.queryParams && router.queryParams.users) {
                    var users = router.queryParams.users;
                    if (typeof users === 'string') {
                        users = [users];
                    }
                    users.forEach(function (user) {
                        var rowIndex = userDirectoryExtUsersStore.findExact('name', user);
                        if (rowIndex != -1) {
                            userDirectoryExtUsersStore.removeAt(rowIndex);
                        }
                    });
                }

                allUsersView.setLoading(false);
            }
        });
    },
	
	showSelectGroups: function (userDirectoryId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            allUsersForm, cancelAddExtUserButton, addExtGroupsGrid,
            userDirectoryExtGroupsStore = me.getStore('Usr.store.MgmUserDirectoryExtGroups'),
            userDirectoryGroupsStore = me.getStore('Usr.store.MgmUserDirectoryGroups'),
            allGroupsView = Ext.create('Usr.view.userDirectory.AddGroupsSetup', {userDirectoryId: userDirectoryId});

        cancelAddExtGroupButton = allGroupsView.down('#grd-add-ext-groups #btn-cancel-add-ext-groups');
        cancelAddExtGroupButton.href = router.getRoute('administration/userdirectories/synchronize').buildUrl({userDirectoryId: userDirectoryId})

        userDirectoryExtGroupsStore.loadData([], false);
        me.getApplication().fireEvent('changecontentevent', allGroupsView);
        addExtGroupsGrid = me.getAddExtGroupsGrid();

        userDirectoryExtGroupsStore.getProxy().setUrl(userDirectoryId);
        userDirectoryExtGroupsStore.load({
            callback: function (records, operation, success) {

                if (router.queryParams && router.queryParams.groups) {
                    var groups = router.queryParams.groups;
                    if (typeof groups === 'string') {
                        groups = [groups];
                    }
                    groups.forEach(function (group) {
                        var rowIndex = userDirectoryExtGroupsStore.findExact('name', group);
                        if (rowIndex != -1) {
                            userDirectoryExtGroupsStore.removeAt(rowIndex);
                        }
                    });
                }

                allGroupsView.setLoading(false);
            }
        });
    },

    saveUsers: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            userDirectoryUsersStore = me.getStore('Usr.store.MgmUserDirectoryUsers'),
			userDirectoryGroupsStore = me.getStore('Usr.store.MgmUserDirectoryGroups'),
            usersList = [], groupsList = [];

        var users = Ext.create(Usr.model.MgmUserDirectoryUsers);

        userDirectoryUsersStore.each(function (record) {
            var user = Ext.create(Usr.model.MgmUserDirectoryUser);
            user.set('name', record.get('name'));
            user.set('status', record.get('status'));

            usersList.push(user);
        });
		
		var userDirectory = me.getModel('Usr.model.MgmUserDirectory');
        userDirectory.load(router.arguments.userDirectoryId, {
            success: function (userDirectoryRecord) {
				var baseGroupValue = userDirectoryRecord.get('baseGroup');
				var groupPanelVisible = baseGroupValue && baseGroupValue.trim() !== '';
				
				users.ldapUsers().add(usersList);
				users.getProxy().setUrl(router.arguments.userDirectoryId);
				users.save({
					success: function (usrRecord) {
						if(groupPanelVisible) {
							
							var groups = Ext.create(Usr.model.MgmUserDirectoryGroups);
							userDirectoryGroupsStore.each(function (record) {
								var group = Ext.create(Usr.model.MgmUserDirectoryGroup);
								group.set('name', record.get('name'));
								group.set('description', record.get('description'));
								groupsList.push(group);
							});
							groups.ldapGroups().add(groupsList);
							groups.save({
								success: function(grpRecord) {
									me.userDirectoryUsersStoreLoaded = false;
									me.userDirectoryGroupsStoreLoaded = false;
									router.getRoute('administration/userdirectories').forward();
								},
								failure: function(grpRecord, operation) {
									
								}
							});
						}
						else {
							me.userDirectoryUsersStoreLoaded = false;
							me.userDirectoryGroupsStoreLoaded = false;
							router.getRoute('administration/userdirectories').forward();
						}
					},
					failure: function (usrRecord, operation) {
					}
				});
			}
		});
    },

    validateSelectedUsers: function(addedUsers, existUsers) {
        var me = this;
        var existUserSet = new Set();
        for (key in existUsers) {
            existUserSet.add(existUsers[key].data.name);
        }

        var addedUserSet = new Set();
        for (key in addedUsers) {
            addedUserSet.add(addedUsers[key].data.name);
        }

        function intersection(addedUserSet, existUserSet) {
            var intersectionSet = new Set();
            var name;
            addedUserSet.forEach(function(name){
                if (existUserSet.has(name)) {
                    intersectionSet.add(name);
                }
            });
            return intersectionSet;
        }

        if (addedUserSet.size !== addedUsers.length || Boolean(intersection(addedUserSet, existUserSet).size)){
            return false;
        } else {
            return true;
        }
    },

    addExtUsers: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            userDirectoryUsersStore = me.getStore('Usr.store.MgmUserDirectoryUsers');
        var addedUsers = me.getAddExtUsersGrid().getSelectedItems();
        var existUsers = userDirectoryUsersStore.getRange();

        if (!me.validateSelectedUsers(addedUsers, existUsers)) {
            var errorWindow = Ext.create('Uni.view.window.Confirmation', {
                noConfirmBtn: true
            });
            errorWindow.show({
                title: Uni.I18n.translate('userDirectories.title.error', 'USR', "Couldn't perform your action"),
                msg: Uni.I18n.translate(
                    'userDirectories.add.user.error.msg',
                    'USR',
                    "The users can't be added since their names must be unique within the domain"
                )
            });
            return;
        }


        if (!me.userDirectoryUsersStoreLoaded) {
            userDirectoryUsersStore.getProxy().setUrl(router.arguments.userDirectoryId);
            userDirectoryUsersStore.load({
                callback: function (records, operation, success) {
                    me.mergeUsersList();
                }
            });
        }
        else {
            me.mergeUsersList();
        }
    },

    mergeUsersList: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            userDirectoryExtUsersStore = me.getStore('Usr.store.MgmUserDirectoryExtUsers'),
            userDirectoryUsersStore = me.getStore('Usr.store.MgmUserDirectoryUsers');

        me.userDirectoryUsersStoreLoaded = true;
        addExtUsersGrid = me.getAddExtUsersGrid();
        userDirectoryExtUsersStore.each(function (record) {

            if (addExtUsersGrid.getSelectionModel().isSelected(record)) {
                if (userDirectoryUsersStore.findExact('name', record.get('name')) == -1) {
                    var user = Ext.create('Usr.model.MgmUserDirectoryUser');
                    user.set('name', record.get('name'));
                    user.set('dn', record.get('dn'));
                    user.set('status', false);
                    user.set('statusDisplay', Uni.I18n.translate('userDirectories.userStatus.inactive', 'USR', 'Inactive'));
                    userDirectoryUsersStore.add(user);
                }
            }
        });

        router.getRoute('administration/userdirectories/synchronize').forward({userDirectoryId: router.arguments.userDirectoryId});
    },
	
	addExtGroups: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            userDirectoryGroupsStore = me.getStore('Usr.store.MgmUserDirectoryGroups');

        if (!me.userDirectoryGroupsStoreLoaded) {
            userDirectoryGroupsStore.getProxy().setUrl(router.arguments.userDirectoryId);
            userDirectoryGroupsStore.load({
                callback: function (records, operation, success) {
                    me.mergeGroupsList();
                }
            });
        }
        else {
            me.mergeGroupsList();
        }
    },

    mergeGroupsList: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            userDirectoryExtGroupsStore = me.getStore('Usr.store.MgmUserDirectoryExtGroups'),
            userDirectoryGroupsStore = me.getStore('Usr.store.MgmUserDirectoryGroups');

        me.userDirectoryGroupsStoreLoaded = true;
        addExtGroupsGrid = me.getAddExtGroupsGrid();
        userDirectoryExtGroupsStore.each(function (record) {

            if (addExtGroupsGrid.getSelectionModel().isSelected(record)) {
                if (userDirectoryGroupsStore.findExact('name', record.get('name')) == -1) {
                    var group = Ext.create('Usr.model.MgmUserDirectoryGroup');
                    group.set('name', record.get('name'));
					group.set('description', record.get('description'));
                    userDirectoryGroupsStore.add(group);
                }
            }
        });

        router.getRoute('administration/userdirectories/synchronize').forward({userDirectoryId: router.arguments.userDirectoryId});
    },

    displayInfo: function (panel) {
        infoDialog = Ext.create('widget.window', {
            title: Uni.I18n.translate('userDirectories.userInfoTitle', 'USR', 'LDAP user info'),
            closable: true,
            overflowY: 'auto',
            modal: true,
            width: 420,
            height: 120,
            layout: {
                type: 'border',
                padding: 5
            },
            items: [
                {
                    xtype: 'container',
                    padding: '0 0 0 5',
                    html: Uni.I18n.translate('userDirectories.userInfoContent', 'USR', 'An LDAP username with sufficient privileges to view the sections of the directory that contain the information for LDAP users.')
                }
            ]
        });

        infoDialog.show();
    },
	
	displayExtendedInfo: function (panel) {
        infoDialog = Ext.create('widget.window', {
            title: Uni.I18n.translate('userDirectories.userInfoTitle', 'USR', 'LDAP user info'),
            closable: true,
            overflowY: 'auto',
            modal: true,
            width: 420,
            height: 160,
            layout: {
                type: 'border',
                padding: 5
            },
            items: [
                {
                    xtype: 'container',
                    html: Uni.I18n.translate('userDirectories.userInfoContentExtended', 'USR', 'An LDAP username with sufficient privileges to view the sections of the directory that contain the information for LDAP users. If the user directory has a Group DN instead of a User base DN, the username should be specified by providing its full DN.')
                }
            ]
        });

        infoDialog.show();
    },

    selectUsers: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            userDirectoryUsersStore = me.getStore('Usr.store.MgmUserDirectoryUsers'),
            users = [];

        userDirectoryUsersStore.each(function (record) {
            users.push(record.get('name'));
        });

        router.getRoute('administration/userdirectories/synchronize/addUsers').forward({userDirectoryId: router.arguments.userDirectoryId}, {users: users});
    },
    
    selectGroups: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            userDirectoryGroupsStore = me.getStore('Usr.store.MgmUserDirectoryGroups'),
            groups = [];

        userDirectoryGroupsStore.each(function (record) {
            groups.push(record.get('name'));
        });

        router.getRoute('administration/userdirectories/synchronize/addGroups').forward({userDirectoryId: router.arguments.userDirectoryId}, {groups: groups});
    },

    synchronizeUsers: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            userDirectoryUsersStore = me.getStore('Usr.store.MgmUserDirectoryUsers'),
            userDirectoryExtUsersStore = me.getStore('Usr.store.MgmUserDirectoryExtUsers'),
            userDirectoryId = router.arguments.userDirectoryId;

        userDirectoryExtUsersStore.getProxy().setUrl(userDirectoryId);
        userDirectoryExtUsersStore.load({
            callback: function (records, operation, success) {

                if (!success) {
                    return;
                }
                userDirectoryUsersStore.each(function (record) {
                    var rowIndex = userDirectoryExtUsersStore.findExact('name', record.get('name'));
                    if (rowIndex == -1) {
                        record.beginEdit();
                        record.set('status', false);
                        record.set('statusDisplay', Uni.I18n.translate('userDirectories.userStatus.inactive', 'USR', 'Inactive'));
                        record.endEdit();

                    }

                    else if (record.get('status')) {
                        if (!userDirectoryExtUsersStore.getAt(rowIndex).get('status')) {
                            record.beginEdit();
                            record.set('status', false);
                            record.set('statusDisplay', Uni.I18n.translate('userDirectories.userStatus.inactiveDeleted', 'USR', 'Inactive'));
                            record.endEdit();
                        }
                    }
                });
            }
        });
    }
});