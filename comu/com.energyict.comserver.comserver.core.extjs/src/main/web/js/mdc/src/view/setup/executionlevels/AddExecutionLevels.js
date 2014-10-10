Ext.define('Mdc.view.setup.executionlevels.AddExecutionLevels', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-execution-levels',
    deviceTypeId: null,
    deviceConfigurationId: null,
    securitySettingId: null,
    store: 'AvailableExecLevelsForSecSettingsOfDevConfig',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.executionlevels.AddExecutionLevelsGrid',
        'Uni.view.container.PreviewContainer'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'addExecutionLevelPanel',
            items: [
                {
                    itemId: 'add-execution-level-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true,
                    width: 380
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'add-execution-levels-grid',
                        itemId: 'execution-level-add-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('executionlevels.empty.title', 'MDC', 'No privileges found'),
                        reasons: [
                            Uni.I18n.translate('executionlevels.empty.list.item1', 'MDC', 'All existing privileges are added to this security set.')
                        ]
                    }

                },
                {
                    xtype: 'container',
                    itemId: 'add-execution-level-selection-error',
                    hidden: true,
                    html: '<span style="color: #eb5642">' + Uni.I18n.translate('executionlevels.no.executionlevel.selected', 'MDC', 'Select at least 1 privilege') + '</span>'
                },
                {
                    xtype: 'form',
                    border: false,
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    width: '100%',
                    defaults: {
                        labelWidth: 250
                    },
                    items: [
                        {
                            xtype: 'toolbar',
                            fieldLabel: '&nbsp',
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            width: '100%',
                            items: [
                                {
                                    text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                    xtype: 'button',
                                    action: 'add',
                                    ui: 'action'
                                },
                                {
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                    action: 'cancel',
                                    xtype: 'button',
                                    ui: 'link',
                                    listeners: {
                                        click: {
                                            fn: function () {
                                                window.location.href = '#/administration/devicetypes/' + this.up('add-execution-levels').deviceTypeId + '/deviceconfigurations/' + this.up('add-execution-levels').deviceConfigurationId + '/securitysettings';
                                            }
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    side: [

    ],

    initComponent: function () {
        this.callParent(arguments);
    }

});
