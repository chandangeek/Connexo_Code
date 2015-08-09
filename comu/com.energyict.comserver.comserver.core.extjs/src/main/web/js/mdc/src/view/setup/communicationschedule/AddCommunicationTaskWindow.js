Ext.define('Mdc.view.setup.communicationschedule.AddCommunicationTaskWindow', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.addCommunicationTaskWindow',
    itemId: 'addCommunicationTaskWindow',
    requires: [
        'Mdc.view.setup.communicationschedule.CommunicationTaskSelectionGrid',
        'Mdc.view.setup.communicationschedule.AddCommunicationTaskPreview'
    ],
    ui: 'large',
    title: Uni.I18n.translate('communicationschedule.addCommunicationTasks', 'MDC', 'Add communication tasks'),
    router: null,

    bbar: {
        xtype: 'toolbar',
        items: [
            {
                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                xtype: 'button',
                itemId: 'addCommunicationTasksToSchedule',
                ui: 'action'
            },
            {
                xtype: 'button',
                itemId: 'cancelAddCommunicationTasksToSchedule',
                ui: 'link',
                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'communicationTaskSelectionGrid'
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('communicationschedule.communicationtasks.empty.title', 'MDC', 'No communication tasks found'),
                    reasons: [
                        Uni.I18n.translate('communicationschedule.communicationtasks.empty.list.item1', 'MDC', 'No communication tasks have been defined yet.'),
                        Uni.I18n.translate('communicationschedule.communicationtasks.empty.list.item2', 'MDC', 'All presented communication tasks already added to shared communication schedule.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('communicationschedule.manageCommunicationTasks', 'MDC', 'Manage communication tasks'),
                            href: me.router.getRoute('administration/communicationtasks').buildUrl()
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'addCommunicationTaskPreview'
                }
            }
        ];

        me.callParent(arguments);
    }
});

