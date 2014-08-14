Ext.define('Mdc.view.setup.communicationschedule.AddCommunicationTaskWindow', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addCommunicationTaskWindow',
    itemId: 'addCommunicationTaskWindow',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.CommunicationTasks',
        'Mdc.view.setup.communicationschedule.CommunicationTaskSelectionGrid'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-wrapper',
    //border: 0,
//    region: 'center',

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('communicationschedule.addCommunicationTasks', 'MDC', 'Add communication tasks'),
            items: [
                {
                    xtype: 'communicationTaskSelectionGrid',
                    itemId: 'communicationTaskGridFromSchedule',
                    store: 'CommunicationTasks'
                },
                {
                    xtype: 'addCommunicationTaskPreview'
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'addCommunicationTaskButtonForm',
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
                                    action: 'addAction',
                                    itemId: 'addButton',
                                    ui: 'action'
                                },
                                {
                                    xtype: 'button',
                                    itemId: 'cancelLink',
                                    action: 'cancelAction',
                                    ui: 'link',
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
//                                    handler: function (button) {
//                                        button.up('.window').close();
//                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
        this.down('#topToolbarContainer').add([
            {
                xtype: 'component',
                flex: 1
            },
            {
                xtype: 'button',
                action: 'cancelAction',
                ui: 'link',
                text: Uni.I18n.translate('communicationschedule.manageCommunicationTasks', 'MDC', 'Manage communication tasks'),
                handler: function (button) {
                    location.href = '#/administration/communicationtasks';
                }
            }
        ])
    }

});

