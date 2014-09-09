Ext.define('Bpm.view.instance.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.instanceBrowse',
    itemId: 'instanceBrowse',
    overflowY: 'auto',
    requires: [
        'Bpm.view.instance.List',
        'Bpm.view.instance.Details',
        'Ext.panel.Panel',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('bpm.instance.title', 'BPM', 'Processes'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'instanceList'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('bpm.instance.empty.title', 'BPM', 'No processes found'),
                        reasons: [
                            Uni.I18n.translate('bpm.instance.empty.list.item1', 'BPM', 'BPM engine cannot be reached.'),
                            Uni.I18n.translate('bpm.instance.empty.list.item2', 'BPM', 'No processes have been started yet.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'instanceDetails'
                    }
                }
            ]
        }
    ]
});

