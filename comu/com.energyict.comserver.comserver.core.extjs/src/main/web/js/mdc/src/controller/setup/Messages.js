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
            }
        })
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
    }
});

