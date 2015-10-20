Ext.define('Bpm.view.process.AddProcessesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'usr-add-processes-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Bpm.view.process.AddProcessesGrid'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('general.addProcesses.addProcesses', 'BPM', 'Add processes'),
                itemId: 'pnl-add-processes',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            xtype: 'bpm-add-processes-grid',
                            itemId: 'grd-add-processes',
                            hrefCancel: '',
                            listeners: {
                                selectionchange: {
                                    fn: Ext.bind(me.onSelectionChange, me)
                                }
                            }
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-add-no-processes',
                            title: Uni.I18n.translate('addProcessess.empty.title', 'BPM', 'No processes found'),
                            reasons: [
                                Uni.I18n.translate('addProcesses.empty.list.item1', 'BPM', 'All processes have been added.'),
                                Uni.I18n.translate('addProcesses.empty.list.item2', 'BPM', 'No process have been defined.'),
                                Uni.I18n.translate('addProcesses.empty.list.item3', 'BPM', 'A network error occurred.')
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    onSelectionChange: function (selectionModel, selected) {
        this.down('[action=saveAddedProcesses]').setDisabled(!selected.length);
    }
});