Ext.define('Bpm.view.process.ProcessesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.bpm-processes-grid',
    store: 'Bpm.store.process.Processes',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Bpm.store.process.Processes'
    ],
    viewConfig:{
        markDirty:false
    },
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('bpm.process.name', 'BPM', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('bpm.process.version', 'BPM', 'Version'),
                dataIndex: 'version',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.process.associated', 'BPM', 'Associated to'),
                dataIndex: 'associatedTo',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.process.status', 'BPM', 'Status'),
                dataIndex: 'active',
                flex: 2,
                renderer: function (value, metaData, record) {
                    switch (value) {
                        case 'ACTIVE':
                            return Uni.I18n.translate('bpm.process.active', 'BPM', 'Active');
                            break;
                        case 'INACTIVE':
                            return Uni.I18n.translate('bpm.process.inactive', 'BPM', 'Inactive');
                            break;
                        case 'UNDEPLOYED':
                            return Uni.I18n.translate('bpm.process.undeployed', 'BPM', 'Undeployed');
                            break;
                        default:
                            return value;
                    }
                }

            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Bpm.privileges.BpmManagement.administrateProcesses,
                width: 100,
                menu: {
                    xtype: 'bpm-process-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('bpm.process.pagingtoolbartop.displayMsg', 'BPM', '{0} - {1} of {2} processes'),
                displayMoreMsg: Uni.I18n.translate('bpm.process.pagingtoolbartop.displayMoreMsg', 'BPM', '{0} - {1} of more than {2} processes'),
                emptyMsg: Uni.I18n.translate('bpm.process.pagingtoolbartop.emptyMsg', 'BPM', 'There are no process to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('bpm.process.pagingtoolbarbottom.itemsPerPage', 'BPM', 'Processes per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
