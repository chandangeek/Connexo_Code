Ext.define('Mdc.controller.setup.Messages', {
    extend: 'Ext.app.Controller',
    requires: [],
    views: [
        'Mdc.view.setup.messages.MessagesOverview'
    ],
    stores: [
        'DeviceConfigMessages',
        'MessagesPrivileges'
    ],
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
            widget = Ext.widget('messages-overview', { deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigId });

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
        grid.store.load({
            callback: function (messagesCategories) {
                if (messagesCategories.length > 1) {
                    grid.getSelectionModel().doSelect(0);
                }
            }
        })
    },

    onMessageCategoryChange: function (sm, records) {
        var record = records[0],
            grid = this.getMessagesGrid(),
            gridContainer = grid.ownerCt,
            noItemsFoundPanel = gridContainer.down('no-items-found-panel'),
            menu = this.getMessagesCategoriesActionMenu();

        if (noItemsFoundPanel) noItemsFoundPanel.destroy();
        grid.setVisible(false);
        menu.removeAll(true);

        if (record) {
            var enablements = record['DeviceMessageEnablementsStore'];

            if (enablements && enablements.getRange().length > 1) {
                grid.setTitle(record.get('DeviceMessageCategory'));
                grid.getView().bindStore(record['DeviceMessageEnablementsStore']);

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
                        title: Uni.I18n.translate('messages.grid.empty.title', 'MDC', 'No messages found'),
                        reasons: [
                            Uni.I18n.translate('messages.grid.emptyCmp.item1', 'MDC', 'No messages have been defined yet.'),
                            Uni.I18n.translate('messages.grid.emptyCmp.item2', 'MDC', 'No messages is available for this category.')
                        ]
                    }
                );
            }
        }
    },

    onMessageChange: function (sm, selection) {
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
        Ext.widget('privileges-info-panel');
    },

    onMessagesCategoriesActionMenuClick: function (menu, item) {
        var messagesCategory = this.getMessagesCategoriesGrid().getSelectionModel().getSelection()[0];
        if (messagesCategory) {
            switch (item.action) {
                case 'deactivateAll':
                    this.deactivateAll(messagesCategory);
                    break;
                default:
                    this.showSelectPrivilegesPanel(messagesCategory, item.action);
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
                    this.showSelectPrivilegesPanel(message, item.action);
            }
        }
    },

    showSelectPrivilegesPanel: function (record, action) {
        var me = this,
            isMessageCategory = !Ext.isEmpty(record.get('DeviceMessageCategory')),
            recordName = isMessageCategory ? record.get('DeviceMessageCategory') : record.get('name'),

            selectPrivilegesPanel = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                confirmation: function () {
                    var privileges = this.down('checkboxgroup').getValues().privileges;
                    me[action](record, privileges);
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
                        columns: 1,
                        vertical: true,
                        items: [
                            { boxLabel: 'Level 1', name: 'privileges', inputValue: '1', checked: true },
                            { boxLabel: 'Level 2', name: 'privileges', inputValue: '2', checked: true },
                            { boxLabel: 'Level 3', name: 'privileges', inputValue: '3', checked: true },
                            { boxLabel: 'Level 4', name: 'privileges', inputValue: '4' }
                        ]
                    }
                ]
            })
        );

        selectPrivilegesPanel.show({
            title: isMessageCategory ?
                Uni.I18n.translatePlural('messages.category.selectPrivilegesPanel.title', recordName, 'MDC', "Select privileges of messages of '{0}'") :
                Uni.I18n.translatePlural('messages.selectPrivilegesPanel.title', recordName, 'MDC', "Select privileges for message '{0}'"),

            msg: isMessageCategory ? Uni.I18n.translate('messages.selectPrivilegesPanel.msg', 'MDC', 'The selected privileges will only apply to the messages that weren`t already active.') : ''
        });
    },

    activateAll: function (messagesCategory, privileges) {
        // Should be integrated with REST when it will be ready
        var inactiveEnablements = [];
        Ext.each(messagesCategory.DeviceMessageEnablementsStore.getRange(), function (e) {
            if (!e.get('active')) inactiveEnablements.push(e);
        });
        console.log('List of inactive messages for activation', inactiveEnablements, 'List of privileges', privileges);
    },

    deactivateAll: function (messagesCategory) {
        // Should be integrated with REST when it will be ready
        var activeEnablements = [];
        Ext.each(messagesCategory.DeviceMessageEnablementsStore.getRange(), function (e) {
            if (e.get('active')) activeEnablements.push(e);
        });
        console.log('List of active messages for deactivation', activeEnablements);
    },

    changePrivilegesForAll: function (messagesCategory, privileges) {
        // Should be integrated with REST when it will be ready
        var activeEnablements = [];
        Ext.each(messagesCategory.DeviceMessageEnablementsStore.getRange(), function (e) {
            if (e.get('active')) activeEnablements.push(e);
        });
        console.log('List of active messages for change privileges', activeEnablements, 'List of privileges', privileges);
    },

    activate: function (message, privileges) {
        // Should be integrated with REST when it will be ready
        console.log('Message for activation', message, 'List of privileges', privileges);
    },

    deactivate: function (message) {
        // Should be integrated with REST when it will be ready
        console.log('Message for deactivation', message);
    },

    changePrivileges: function (message, privileges) {
        // Should be integrated with REST when it will be ready
        console.log('Message for change privileges', message, 'List of privileges', privileges);
    }
});

