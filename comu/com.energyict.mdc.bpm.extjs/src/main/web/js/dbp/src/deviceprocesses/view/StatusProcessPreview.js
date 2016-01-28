Ext.define('Dbp.deviceprocesses.view.StatusProcessPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.dbp-status-process-preview',
    requires: [
        'Dbp.deviceprocesses.view.VariablesPreview'
    ],
    layout: {
        type: 'column',
        align: 'stretch'
    },
    items: [
        {
            xtype: 'gridpanel',
            itemId: 'process-nodes-grid',
            columnWidth: 0.55,
            columns: {
                items: [
                    {
                        header: Uni.I18n.translate('dbp.process.node.status', 'MDC', ' Status'),
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
                        },
                    },
                    {
                        header: Uni.I18n.translate('dbp.process.node.node', 'MDC', ' Node'),
                        dataIndex: 'name',
                        flex: 2
                    },
                    {
                        header: Uni.I18n.translate('dbp.process.node.startedOn', 'MDC', ' Started on'),
                        dataIndex: 'logDateDisplay',
                        flex: 2
                    },
                    {
                        header: Uni.I18n.translate('dbp.process.node.type', 'MDC', ' Type'),
                        dataIndex: 'type',
                        flex: 2
                    }
                ]
            }
        },
        {
            xtype: 'dbp-node-variables-preview',
            columnWidth: 0.45,
            itemId: 'node-variables-preview-panel',
            margin: '0 0 0 20'
        }
    ]
});

