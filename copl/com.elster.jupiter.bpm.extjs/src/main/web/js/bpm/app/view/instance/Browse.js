Ext.define('Bpm.view.instance.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.instanceBrowse',
    itemId: 'instanceBrowse',
    overflowY: 'auto',
    requires: [
        'Bpm.view.instance.List',
        'Bpm.view.instance.Details',
        'Ext.panel.Panel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('bpm.processInstances.title', 'BPM', 'Process instances'),
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
                        xtype: 'component',
                        html: 'There are no process instances'
                    },
                    previewComponent: {
                        xtype: 'instanceDetails'
                    }
                }
            ]
        }
    ]
});

