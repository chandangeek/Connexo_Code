Ext.define('Bpm.monitorprocesses.view.StatusProcessPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.bpm-status-process-preview',
    layout: {
        type: 'column',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'gridpanel',
            maxHeight: 366,
            itemId: 'process-nodes-grid',
            columnWidth: 0.55,
            columns: {
                items: [
                    {
                        header: Uni.I18n.translate('bpm.process.node.status', 'MDC', ' Status'),
                        dataIndex: 'status',
                        flex: 1,
                        renderer: function (value,metaData) {
                            metaData.tdCls = 'communication-tasks-status';
                            var template = '';

                            switch(value) {
                                case 'COMPLETED':
                                    template += '<tpl><span class="icon-checkmark"></span>';
                                    break;
                                case 'ACTIVE':
                                    template += '<tpl><span class="icon-inprogress"></span>';
                                    break;
                                case 'ABORTED':
                                    template += '<tpl><span class="icon-close"></span>';
                                    break;
                            }
                            return template;
                        }
                    },
                    {
                        header: Uni.I18n.translate('bpm.process.node.node', 'MDC', ' Node'),
                        dataIndex: 'name',
                        flex: 2
                    },
                    {
                        header: Uni.I18n.translate('bpm.process.node.startedOn', 'MDC', ' Started on'),
                        dataIndex: 'logDateDisplay',
                        flex: 2
                    },
                    {
                        header: Uni.I18n.translate('bpm.process.node.type', 'MDC', ' Type'),
                        dataIndex: 'type',
                        flex: 2
                    }
                ]
            }
        },
        {
            xtype: 'panel',
            columnWidth: 0.45,
            itemId: 'node-variables-preview-panel',
            margin: '0 0 0 20',
            frame: true,
            autoScroll: true,
            header : {
                height : 32
            },
            items:[]
        }
    ]
});

