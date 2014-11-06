Ext.define('Mdc.controller.setup.Messages', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.model.MessageCategory',
        'Mdc.model.MessageActivate',
        'Mdc.store.MessagesPrivileges',
        'Mdc.store.MessagesGridStore'
    ],
    views: [
        'Mdc.view.setup.messages.MessagesOverview'
    ],
    stores: [
        'DeviceConfigMessages',
        'MessagesPrivileges',
        'MessagesGridStore'
    ],
    models: [ 'Mdc.model.MessageCategory' ],
    refs: [
        { ref: 'messagesCategoriesGrid', selector: 'messages-categories-grid' },
        { ref: 'messagesCategoriesActionMenu', selector: '#messages-categories-actionmenu' },
        { ref: 'messagesGrid', selector: 'messages-grid' },
        { ref: 'messagesActionMenu', selector: '#messages-actionmenu' },
        { ref: 'messagesActionBtn', selector: '#messages-actionbutton' }
    ],
    deviceTypeId: null,
    deviceConfigId: null,

    init: function () {
        this.callParent(arguments);
        this.control({
            'messages-categories-grid': {
                afterrender: this.onMessagesCategoriesGridAfterRender,
                selectionchange: this.onMessageCategoryChange
            },
            'messages-grid': {
                selectionchange: this.onMessageChange
            },
            'button[name=messageInfoIcon]': {
                click: this.onMessagesGridInfoIconClick
            },
            '#messages-categories-actionmenu': {
                click: this.onMessagesCategoriesActionMenuClick
            },
            '#messages-actionmenu': {
                click: this.onMessagesActionMenuClick
            }
        });
    },

    showMessagesOverview: function (deviceTypeId, deviceConfigId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.MessageCategory');
        model.getProxy().setExtraParam('deviceType', deviceTypeId);
        model.getProxy().setExtraParam('deviceConfig', deviceConfigId);

        var  widget = Ext.widget('messages-overview', { deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigId });
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigId = deviceConfigId;
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    onMessagesCategoriesGridAfterRender: function (grid) {
        var model = Ext.ModelManager.getModel('Mdc.model.MessageCategory');
        model.getProxy().setExtraParam('deviceType', grid.deviceTypeId);
        model.getProxy().setExtraParam('deviceConfig', grid.deviceConfigId);
        grid.store.load({
            callback: function (messagesCategories) {
                if (messagesCategories && messagesCategories.length > 0) {
                    grid.getSelectionModel().doSelect(0);
                }
            }
        })
    },

    onMessageCategoryChange: function (sm, records) {
        var router = this.getController('Uni.controller.history.Router'),
            model = Ext.ModelManager.getModel('Mdc.model.MessageCategory');

        model.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        model.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);

        var record = records[0],
            grid = this.getMessagesGrid(),
            gridContainer = grid.ownerCt,
            noItemsFoundPanel = grid.down('no-items-found-panel'),
            menu = this.getMessagesCategoriesActionMenu();
        if (noItemsFoundPanel) noItemsFoundPanel.destroy();
        grid.setVisible(false);
        menu.removeAll(true);
        grid.getStore().removeAll();
        if (record) {
            var enablements = record['deviceMessageEnablementsStore'];
            if (enablements && enablements.getRange().length > 0) {
                var store = grid.getStore();
                grid.setTitle(Uni.I18n.translate('commands.titleof', 'MDC', 'Commands of ') + record.get('name'));

                record['deviceMessageEnablementsStore'].each(function (rec) {
                    store.add(rec);
                });

                grid.down('pagingtoolbartop').store = store;
                grid.down('pagingtoolbartop').store.totalCount = store.getCount();
                grid.down('pagingtoolbartop').displayMsg = Uni.I18n.translatePlural('commands.commands', store.getCount(), 'MDC', '{2} commands'),
                grid.down('pagingtoolbartop').updateInfo();

                Ext.defer(function () {
                    grid.doLayout();
                    grid.setVisible(true);
                }, 250);

                var activeEnablements = 0,
                    inactiveEnablements = 0;

                Ext.each(enablements.getRange(), function (e) {
                    e.get('active') ? activeEnablements++ : inactiveEnablements++;
                });

                if (inactiveEnablements > 0) {
                    menu.add(
                        {
                            text: Uni.I18n.translate('messages.categories.activateAll', 'MDC', 'Activate all'),
                            itemId: 'activateAll',
                            action: 'activateAll'
                        }
                    );
                }

                if (activeEnablements > 0) {
                    menu.add(
                        {
                            text: Uni.I18n.translate('messages.categories.deactivateAll', 'MDC', 'Deactivate all'),
                            itemId: 'deactivateAll',
                            action: 'deactivateAll'
                        },
                        {
                            text: Uni.I18n.translate('messages.categories.changePrivilegesForAll', 'MDC', 'Change privileges for all'),
                            itemId: 'changePrivilegesForAll',
                            action: 'changePrivilegesForAll'
                        }
                    );
                }
                this.getMessagesActionBtn().menu = menu;
            } else {
                gridContainer.add(
                    {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('commands.grid.empty.title', 'MDC', 'Device protocol did not specify any commands.'),
                        reasons: [
                            Uni.I18n.translate('commands.grid.emptyCmp.item1', 'MDC', 'No commands have been defined yet.'),
                            Uni.I18n.translate('commands.grid.emptyCmp.item2', 'MDC', 'No commands is available for this category.')
                        ]
                    }
                );
            }
        }
    },

    onMessageChange: function (sm, selection) {
        var me = this;
        var record = selection[0],
            menu = this.getMessagesActionMenu();

        menu.removeAll(true);

        if (record && record.get('active')) {
            menu.add(
                {
                    text: Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate'),
                    itemId: 'deactivate',
                    action: 'deactivate'
                },
                {
                    text: Uni.I18n.translate('messages.categories.changePrivileges', 'MDC', 'Change privileges'),
                    itemId: 'changePrivileges',
                    action: 'changePrivileges'
                }
            );
        } else {
            menu.add(
                {
                    text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                    itemId: 'activate',
                    action: 'activate'
                }
            );
        }
    },

    onMessagesGridInfoIconClick: function () {
        var widget = Ext.widget('privileges-info-panel'),
            dataView = widget.down('dataview');

        dataView.store.load();
    },

    onMessagesCategoriesActionMenuClick: function (menu, item) {
        var messagesCategory = this.getMessagesCategoriesGrid().getSelectionModel().getSelection()[0];
        if (messagesCategory) {
            switch (item.action) {
                case 'deactivateAll':
                    this.deactivateAll(messagesCategory);
                    break;
                default:
                    this.showSelectPrivilegesPanel(messagesCategory, item.action, false);
            }
        }
    },

    onMessagesActionMenuClick: function (menu, item) {
        var message = this.getMessagesGrid().getSelectionModel().getSelection()[0];
        if (message) {
            switch (item.action) {
                case 'deactivate':
                    this.deactivate(message);
                    break;
                default:
                    this.showSelectPrivilegesPanel(message, item.action, true);
            }
        }
    },

    showSelectPrivilegesPanel: function (record, action, setAlreadyChecked) {
        var me = this,
            isMessageCategory = !Ext.isEmpty(record.get('DeviceMessageCategory')),
            recordName = isMessageCategory ? record.get('DeviceMessageCategory') : record.get('name'),
            selectPrivilegesPanel = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                init: function () {
                    var initStore = me.get('deviceMessageEnablements');
                    initStore.load();
                    var initPrivStore = me.get('MessagesPrivileges');
                    initPrivStore.load();
                },
                confirmation: function () {
                    var privileges = this.down('checkboxgroup').getChecked();
                    var privIds = [];
                    if (action == 'changePrivilegesForAll' || action == 'activateAll' || action == 'activate' || action == 'changePrivileges') {
                        Ext.each(privileges, function (privilege) {
                            privIds.push({privilege: privilege.inputValue})
                        })
                        me[action](record, privIds);
                    } else {
                        Ext.each(privileges, function (privilege) {
                            var privilegesStore = me.getMessagesPrivilegesStore();
                            privilegesStore.load(function () {
                                privilegesStore.each(function (privilegeItem) {

                                    if (privilege.boxLabel === privilegeItem.get('name'))
                                        privIds.push({privilege: privilegeItem.get('privilege')});
                                })
                                me[action](record, privIds);
                            })

                        });
                    }

                    this.close();
                },
                listeners: {
                    close: function () {
                        this.destroy()
                    }
                }
            });

        selectPrivilegesPanel.add(

            Ext.create('Ext.container.Container', {
                width: 400,
                items: [
                    {
                        xtype: 'checkboxgroup',
                        fieldLabel: Uni.I18n.translate('messages.selectPrivilegesPanel.label', 'MDC', 'Privileges'),
                        labelAlign: 'left',
                        labelStyle: 'padding-left: 53px',
                        labelPad: 50,
                        store: 'MessagesPrivileges',
                        columns: 1,
                        vertical: true,
                        items: [
                            { boxLabel: 'Level 1', name: 'cbgroup', inputValue: 'execute.device.message.level1'},
                            { boxLabel: 'Level 2', name: 'cbgroup', inputValue: 'execute.device.message.level2'},
                            { boxLabel: 'Level 3', name: 'cbgroup', inputValue: 'execute.device.message.level3'},
                            { boxLabel: 'Level 4', name: 'cbgroup', inputValue: 'execute.device.message.level4'}
                        ]
                    }
                ]
            })
        );

        selectPrivilegesPanel.show(
            {
                title: !setAlreadyChecked ?
                    Uni.I18n.translatePlural('messages.category.selectPrivilegesPanel.title', recordName, 'MDC', "Select privileges of commands of '{0}'") :
                    Uni.I18n.translatePlural('messages.selectPrivilegesPanel.title', recordName, 'MDC', "Select privileges for command '{0}'"),
                msg: setAlreadyChecked ? '' : Uni.I18n.translate('messages.selectPrivilegesPanel.msg', 'MDC', 'The selected privileges will only apply to the commands that already active.')
            });
        if (setAlreadyChecked) {
            var checked = record.get('privileges');
            var checkboxgroup = selectPrivilegesPanel.down('checkboxgroup');
            Ext.each(checkboxgroup.items.items, function (checkbox) {
                for (var i = 0, l = checked.length; i < l; i++) {
                    if (checkbox.inputValue == checked[i].privilege)
                        checkbox.setValue(true);
                }
            });
        }
    },
    activateAll: function (messagesCategory, privileges) {
        var inactiveEnablements = [];
        Ext.each(messagesCategory.deviceMessageEnablementsStore.getRange(), function (e) {
            if (!e.get('active')) {

                inactiveEnablements.push(e.get('id'));
                if (!Ext.isEmpty(e.get('privileges'))) {
                    Ext.each(e.get('privileges'), function (privilege) {
                        privileges.push({privilege: privilege.privilege});
                    });
                }

            }
        });

        var router = this.getController('Uni.controller.history.Router');
        var model = Ext.create('Mdc.model.MessageActivate');
        model.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        model.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        model.set('messageIds', inactiveEnablements);
        model.set('privileges', privileges);
        model.save();
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        this.getMessagesCategoriesGrid().store.load();
    },

    deactivateRequest: function(message) {
        var router = this.getController('Uni.controller.history.Router');
        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + router.arguments.deviceTypeId + '/deviceconfigurations/' + router.arguments.deviceConfigurationId
                + '/devicemessageenablements/?messageId=' + message,
            method: 'DELETE',
            waitMsg: 'Removing...',
            success: function () {
            },
            failure: function (response) {
                var errorText = "Unknown error occurred";

                if (response.status == 400) {
                    var result = Ext.JSON.decode(response.responseText, true);
                    if (result && result.message) {
                        errorText = result.message;
                    }
                }
            }
        });
    },


    deactivateAll: function (messagesCategory) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.MessageCategory');
        var router = this.getController('Uni.controller.history.Router');
        var activeEnablements = [];

        Ext.each(messagesCategory.deviceMessageEnablementsStore.getRange(), function (e) {
            if (e.get('active')) {
                activeEnablements.push(e.get('id'));
            }
        });
        Ext.each(activeEnablements, function(e) {
            me.deactivateRequest(e);
        });

        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        this.getMessagesCategoriesGrid().store.load();

    },

    changePrivilegesForAll: function (messagesCategory, privileges) {
        var activeEnablements = [];
        Ext.each(messagesCategory.deviceMessageEnablementsStore.getRange(), function (e) {
                if (e.get('active')) {
                    activeEnablements.push(e.get('id'));
                }
        });
        this.changeRequest(activeEnablements, privileges);
        var router = this.getController('Uni.controller.history.Router');
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        this.getMessagesCategoriesGrid().store.load();
    },

    activate: function (message, privileges) {
        var messageIds = [];
        messageIds.push(message.get('id'));
        var router = this.getController('Uni.controller.history.Router');
        var model = Ext.create('Mdc.model.MessageActivate');
        model.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        model.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        model.set('messageIds', messageIds);
        model.set('privileges', privileges);
        model.save();

        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        this.getMessagesCategoriesGrid().store.load();
    },

    deactivate: function (message) {
        var messageIds = [];
        messageIds.push(message.get('id'));
        this.deactivateRequest(messageIds);
        var router = this.getController('Uni.controller.history.Router');
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        this.getMessagesCategoriesGrid().store.load();
    },

    changePrivileges: function (message, privileges) {
        var messageIds = [];
        messageIds.push(message.get('id'));
        this.changeRequest(messageIds, privileges);
        var router = this.getController('Uni.controller.history.Router');
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        this.getMessagesCategoriesGrid().store.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        this.getMessagesCategoriesGrid().store.load();

    },
    changeRequest: function(message, privileges) {
        var router = this.getController('Uni.controller.history.Router');
        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + router.arguments.deviceTypeId + '/deviceconfigurations/' + router.arguments.deviceConfigurationId
                + '/devicemessageenablements/',
            method: 'PUT',
            jsonData: {
                "messageIds": message,
                "privileges": privileges
            },
            waitMsg: 'Changing privileges...',
            success: function () {
            },
            failure: function (response) {
                var errorText = "Unknown error occurred";

                if (response.status == 400) {
                    var result = Ext.JSON.decode(response.responseText, true);
                    if (result && result.message) {
                        errorText = result.message;
                    }
                }
            }
        });
    }
});

