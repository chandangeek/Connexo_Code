Ext.define('Mdc.view.setup.comtasks.ComtaskSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comtaskSetup',
    itemId: 'comtaskSetup',
    requires: [
        'Mdc.view.setup.comtasks.ComtaskGrid',
        'Mdc.view.setup.comtasks.ComtaskPreview'
    ],
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('comtask.comtasks', 'MDC', 'Communication tasks'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'comtaskGrid'
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
                                src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        itemId: 'comtaskEmptyCollectionComponent',
                                        html: '<b>' + Uni.I18n.translate('comtask.empty.title', 'MDC', 'No communication tasks found') + '</b><br>' +
                                            Uni.I18n.translate('comtask.empty.detail', 'MDC', 'There are no communication tasks. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('comtask.empty.list.item1', 'MDC', 'No communication tasks have been added yet') + '</li>' +
                                            Uni.I18n.translate('comtask.empty.list.item2', 'MDC', 'No communication tasks comply to the filter') + '</li></lv><br>' +
                                            Uni.I18n.translate('comtask.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'comtaskCreateActionButton',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('comtask.create', 'MDC', 'Add communication task'),
                                        hrefTarget: '',
                                        href: '#/administration/communicationtasks/create'
                                    }
                                ]
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'comtaskPreview'
                    }
                }
            ]
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});


