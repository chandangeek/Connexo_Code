Ext.define('Bpm.view.instance.Node', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.nodeList',
    itemId: 'nodeList',

    requires: [
        'Bpm.store.Nodes'
    ],

    store: undefined,
    title: Uni.I18n.translate('bpm.instance.activities', 'BPM', 'Activities'),
    height: 250,

    initComponent: function () {
        this.columns = {
            defaults: {
                flex: 1,
                sortable: false,
                hideable: false,
                fixed: true
            },
            items: [
                {
                    header: Uni.I18n.translate('bpm.instance.node.name', 'BPM', 'Name'),
                    dataIndex: 'nodeName',
                    flex: 2
                },
                {
                    header: Uni.I18n.translate('bpm.instance.node.date', 'BPM', 'Start date'),
                    dataIndex: 'date',
                    flex: 2
                },
                {
                    header: Uni.I18n.translate('bpm.instance.node.type', 'BPM', 'Activity type'),
                    dataIndex: 'nodeType',
                    flex: 2
                },
                {
                    header: Uni.I18n.translate('bpm.instance.node.state', 'BPM', 'State'),
                    dataIndex: 'state',
                    renderer: function (value) {
                        if (value == 0) {
                            return Uni.I18n.translate('bpm.instance.node.state.progress', 'BPM', 'In progress');
                        } else {
                            return Uni.I18n.translate('bpm.instance.node.state.completed', 'BPM', 'Completed');
                        }
                    },
                    flex: 2
                }
            ]
        };

        this.callParent();
    }
});
