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
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'left'
                        },
                        minHeight: 20,
                        items: [
                            {
                                xtype: 'image',
                                margin: '0 10 0 0',
                                src: "../mdc/resources/images/information.png",
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: '<b>' + Uni.I18n.translate('bpm.instance.empty.title', 'BPM', 'No processes found') + '</b><br>' +
                                            Uni.I18n.translate('bpm.instance.empty.detail', 'BPM', 'There are no processes. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('bpm.instance.empty.list.item1', 'BPM', 'BPM engine cannot be reached.') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('bpm.instance.empty.list.item2', 'BPM', 'No processes have been started yet.')
                                    }
                                ]
                            }
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

