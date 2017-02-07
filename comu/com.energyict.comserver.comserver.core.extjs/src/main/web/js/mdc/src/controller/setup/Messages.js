/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    models: [
        'Mdc.model.DeviceType',
        'Mdc.model.DeviceConfiguration',
        'Mdc.model.MessageCategory'
    ],
    refs: [
        { ref: 'messagesCategoriesGrid', selector: 'messages-categories-grid' },
        { ref: 'messagesCategoriesActionMenu', selector: '#messages-categories-actionmenu' },
        { ref: 'messagesGrid', selector: 'messages-grid' },
        { ref: 'messagesActionMenu', selector: '#messages-actionmenu' },
        { ref: 'messagesActionBtn', selector: '#messages-actionbutton' },
        { ref: 'messagesOverview', selector: 'messages-overview' }
    ],
    deviceTypeId: null,
    deviceConfigId: null,
    recordName: '',
    isCategory: false,

    init: function () {
        this.callParent(arguments);
        this.control({
            'messages-categories-grid': {
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
            models = {
                messageCategory: me.getModel('Mdc.model.MessageCategory'),
                deviceType: me.getModel('Mdc.model.DeviceType'),
                deviceConfiguration: me.getModel('Mdc.model.DeviceConfiguration')
            },
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        models.messageCategory.getProxy().setExtraParam('deviceType', deviceTypeId);
        models.messageCategory.getProxy().setExtraParam('deviceConfig', deviceConfigId);

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigId = deviceConfigId;

        models.deviceType.load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
            }
        });

        models.deviceConfiguration.getProxy().setExtraParam('deviceType', deviceTypeId);
        models.deviceConfiguration.load(deviceConfigId, {
            success: function (deviceConfig) {
                var  widget = Ext.widget('messages-overview', {
                    deviceTypeId: deviceTypeId,
                    deviceConfigId: deviceConfigId
                });

                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                widget.deviceConfiguration = deviceConfig;
                widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfig.get('name'));
                me.getApplication().fireEvent('changecontentevent', widget);
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
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
        if(menu)
            menu.removeAll(true);
        grid.getStore().removeAll();
        if (record) {
            var enablements = record['deviceMessageEnablementsStore'];
            if (enablements && enablements.getRange().length > 0) {
                var store = grid.getStore();
                grid.setTitle(Uni.I18n.translate('commands.titleof', 'MDC', "Commands of '{0}'",[record.get('name')]));

                record['deviceMessageEnablementsStore'].each(function (rec) {
                    store.add(rec);
                });

                grid.down('pagingtoolbartop').store = store;
                grid.down('pagingtoolbartop').store.totalCount = store.getCount();
                grid.down('pagingtoolbartop').displayMsg = Uni.I18n.translatePlural('commands.commands', store.getCount(), 'MDC', 'No commands', '{0} command', '{0} commands');
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

                if(menu) {
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
                        Ext.suspendLayouts();
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
                        Ext.resumeLayouts();
                    }

                    this.getMessagesActionBtn().menu = menu;
                }
            } else {
                gridContainer.add(
                    {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('commands.grid.empty.title', 'MDC', 'Device protocol did not specify any commands.'),
                        reasons: [
                            Uni.I18n.translate('commands.grid.emptyCmp.item1', 'MDC', 'No commands have been defined yet.'),
                            Uni.I18n.translate('commands.grid.emptyCmp.item2', 'MDC', 'No commands are available for this category.')
                        ]
                    }
                );
            }
        }
    },

    onMessageChange: function (sm, selection) {
        var record = selection[0],
            menu = this.getMessagesActionMenu();

        if(menu) {
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
        }
    },

    onMessagesGridInfoIconClick: function (button) {
        var widget = Ext.widget('privileges-info-panel'),
            dataView = widget.down('dataview');
        dataView.store.load();
    },

    onMessagesCategoriesActionMenuClick: function (menu, item) {
        var me = this,
            messagesCategory = this.getMessagesCategoriesGrid().getSelectionModel().getSelection()[0];
        if (messagesCategory) {
            me.recordName = !Ext.isEmpty(messagesCategory.get('DeviceMessageCategory')) ? messagesCategory.get('DeviceMessageCategory') : messagesCategory.get('name');
            me.isCategory = true;
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
        var me = this,
            message = this.getMessagesGrid().getSelectionModel().getSelection()[0];
        if (message) {
            me.recordName = !Ext.isEmpty(message.get('DeviceMessageCategory')) ? message.get('DeviceMessageCategory') : message.get('name');
            me.isCategory = false;
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
            selectPrivilegesPanel = Ext.create('Ext.window.Window', {
                title: !setAlreadyChecked ?
                    Uni.I18n.translate('messages.category.selectPrivilegesPanel.title', 'MDC', "Select privileges of '{0}' commands", [recordName]) :
                    Uni.I18n.translate('messages.selectPrivilegesPanel.title', 'MDC', "Select privileges for command '{0}'", [recordName]),
                modal: true,
                closeAction: 'destroy',
                buttons: [
                    {
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                        ui: 'action',
                        handler: function () {
                            var privileges = this.up('window').down('checkboxgroup').getChecked();
                            var privIds = [];
                            if (action == 'changePrivilegesForAll' || action == 'activateAll' || action == 'activate' || action == 'changePrivileges') {
                                Ext.each(privileges, function (privilege) {
                                    privIds.push({privilege: privilege.inputValue})
                                });
                                me[action](record, privIds);
                            } else {
                                Ext.each(privileges, function (privilege) {
                                    var privilegesStore = me.getMessagesPrivilegesStore();
                                    privilegesStore.load(function () {
                                        privilegesStore.each(function (privilegeItem) {

                                            if (privilege.boxLabel === privilegeItem.get('name'))
                                                privIds.push({privilege: privilegeItem.get('privilege')});
                                        });
                                        me[action](record, privIds);
                                    })

                                });
                            }

                            this.up('window').close();
                        }
                    },
                    {
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        ui: 'link',
                        handler: function () {
                            this.up('window').close();
                        }
                    }
                ],
                init: function () {
                    var initStore = me.get('deviceMessageEnablements');
                    initStore.load();
                    var initPrivStore = me.get('MessagesPrivileges');
                    initPrivStore.load();
                }
            }),
            warningText;



        if (!setAlreadyChecked) {
            switch (action) {
                case 'activateAll':
                    selectPrivilegesPanel.setTitle(
                        Uni.I18n.translate('messages.category.selectPrivilegesPanel.title', 'MDC', "Select privileges of '{0}' commands", [recordName])
                    );
                    warningText = Uni.I18n.translate('messages.selectPrivilegesPanelChange.msg', 'MDC', 'The selected privileges will only apply to the commands that aren\'t active yet.');
                    break;
                case 'changePrivilegesForAll':
                    selectPrivilegesPanel.setTitle(
                        Uni.I18n.translate('messages.category.changePrivilegesPanel.title', 'MDC', "Change privileges of '{0}' commands", [recordName])
                    );
                    warningText = Uni.I18n.translate('messages.selectPrivilegesPanel.msg', 'MDC', 'The selected privileges will only apply to the commands that are active.');
                    break;
            }
            selectPrivilegesPanel.add({
                xtype: 'component',
                html: Ext.String.htmlEncode(warningText)
            });
        } else {
            selectPrivilegesPanel.setTitle(
                Uni.I18n.translate('messages.selectPrivilegesPanel.title', 'MDC', "Select privileges for command '{0}'", [recordName])
            );
        }

        selectPrivilegesPanel.add(
            {
                xtype: 'checkboxgroup',
                width: 400,
                fieldLabel: Uni.I18n.translate('messages.selectPrivilegesPanel.label', 'MDC', 'Privileges'),
                labelAlign: 'left',
                labelStyle: 'padding-left: 53px',
                labelPad: 50,
                store: 'MessagesPrivileges',
                columns: 1,
                vertical: true,
                items: [
                    { boxLabel: 'Level 1', name: 'cbgroup', inputValue: 'execute.device.message.level1', checked: true},
                    { boxLabel: 'Level 2', name: 'cbgroup', inputValue: 'execute.device.message.level2', checked: true},
                    { boxLabel: 'Level 3', name: 'cbgroup', inputValue: 'execute.device.message.level3', checked: true},
                    { boxLabel: 'Level 4', name: 'cbgroup', inputValue: 'execute.device.message.level4'}
                ]
            }
        );

        selectPrivilegesPanel.show();
        if (action === 'changePrivileges') {
            Ext.Array.each(selectPrivilegesPanel.down('checkboxgroup').query('checkbox'), function (checkbox) {
                var privilege = Ext.Array.findBy(record.get('privileges'), function (item) {
                    return item.privilege === checkbox.inputValue;
                });
                checkbox.setValue(privilege ? true : false);
            });
        }
    },
    activateAll: function (messagesCategory, privileges) {
        var me = this,
            inactiveEnablements = [],
            router = this.getController('Uni.controller.history.Router'),
            model = Ext.create('Mdc.model.MessageActivate'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();

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

        model.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        model.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        model.beginEdit();
        model.set('messageIds', inactiveEnablements);
        model.set('privileges', privileges);
        model.set('deviceConfiguration', me.getMessagesOverview().deviceConfiguration.getRecordData());
        model.endEdit();
        model.save({
            isNotEdit: true,
            success: function () {
                if (me.isCategory) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceConfig.commands.activateCategory.acknowledge', 'MDC', "Privileges of '{0}' commands activated", [me.recordName]));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceConfig.commands.activateCommand.acknowledge', 'MDC', "Privileges for command '{0}' activated", [me.recordName]));
                }
                router.getRoute().forward();
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    },

    deactivateRequest: function(message) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + router.arguments.deviceTypeId + '/deviceconfigurations/' + router.arguments.deviceConfigurationId
                + '/devicemessageenablements',
            method: 'DELETE',
            waitMsg: 'Removing...',
            isNotEdit: true,
            jsonData: {
                messageIds: message,
                deviceConfiguration: me.getMessagesOverview().deviceConfiguration.getRecordData()
            },
            success: function () {
                if (me.isCategory) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceConfig.commands.deactivateCategory.acknowledge', 'MDC', "Privileges of '{0}' commands deactivated", [me.recordName]));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceConfig.commands.deactivateCommand.acknowledge', 'MDC', "Privileges for command '{0}' deactivated", [me.recordName]));
                }
                router.getRoute().forward();
            },
            failure: function (response) {
                var errorText = "Unknown error occurred";

                if (response.status == 400) {
                    var result = Ext.JSON.decode(response.responseText, true);
                    if (result && result.message) {
                        errorText = result.message;
                    }
                }
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    },


    deactivateAll: function (messagesCategory) {
        var me = this,
            activeEnablements = [];

        Ext.each(messagesCategory.deviceMessageEnablementsStore.getRange(), function (e) {
            if (e.get('active')) {
                activeEnablements.push(e.get('id'));
            }
        });
        me.deactivateRequest(activeEnablements);
    },

    changePrivilegesForAll: function (messagesCategory, privileges) {
        var activeEnablements = [];
        Ext.each(messagesCategory.deviceMessageEnablementsStore.getRange(), function (e) {
                if (e.get('active')) {
                    activeEnablements.push(e.get('id'));
                }
        });
        this.changeRequest(activeEnablements, privileges);
    },

    activate: function (message, privileges) {
        var me = this,
            messageIds = [],
            router = this.getController('Uni.controller.history.Router'),
            model = Ext.create('Mdc.model.MessageActivate'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        messageIds.push(message.get('id'));
        model.getProxy().setExtraParam('deviceType', router.arguments.deviceTypeId);
        model.getProxy().setExtraParam('deviceConfig', router.arguments.deviceConfigurationId);
        model.set('messageIds', messageIds);
        model.set('privileges', privileges);
        model.set('deviceConfiguration', me.getMessagesOverview().deviceConfiguration.getRecordData());
        model.save({
            isNotEdit: true,
            success: function () {
                if (me.isCategory) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceConfig.commands.activateCategory.acknowledge', 'MDC', "Privileges of '{0}' commands activated", [me.recordName]));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceConfig.commands.activateCommand.acknowledge', 'MDC', "Privileges for command '{0}' activated", [me.recordName]));
                }
                router.getRoute().forward();
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    },

    deactivate: function (message) {
        var me = this,
            messageIds = [];

        messageIds.push(message.get('id'));
        me.deactivateRequest(messageIds);
    },

    changePrivileges: function (message, privileges) {
        var me = this,
            messageIds = [];

        messageIds.push(message.get('id'));
        me.changeRequest(messageIds, privileges);
    },
    changeRequest: function(message, privileges) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + router.arguments.deviceTypeId + '/deviceconfigurations/' + router.arguments.deviceConfigurationId
                + '/devicemessageenablements/',
            method: 'PUT',
            isNotEdit: true,
            jsonData: {
                "messageIds": message,
                "privileges": privileges,
                deviceConfiguration: me.getMessagesOverview().deviceConfiguration.getRecordData()
            },
            waitMsg: 'Changing privileges...',
            success: function () {
                if (me.isCategory) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceConfig.commands.changeCategory.acknowledge', 'MDC', "Privileges of '{0}' commands changed", [me.recordName]));
                } else {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceConfig.commands.changeCommand.acknowledge', 'MDC', "Privileges for command '{0}' changed", [me.recordName]));
                }
                router.getRoute().forward();
            },
            failure: function (response) {
                var errorText = "Unknown error occurred";

                if (response.status == 400) {
                    var result = Ext.JSON.decode(response.responseText, true);
                    if (result && result.message) {
                        errorText = result.message;
                    }
                }
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    }
});

