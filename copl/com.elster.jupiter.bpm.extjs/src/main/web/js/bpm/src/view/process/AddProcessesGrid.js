Ext.define('Bpm.view.process.AddProcessesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    xtype: 'bpm-add-processes-grid',
    store: 'Bpm.store.process.BpmProcesses',
    height: 310,
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfProcesses.selected', count, 'BPM',
            'No process selected', '{0} process selected', '{0} processes selected'
        );
    },

    columns: {
        items: [
            {
                header: Uni.I18n.translate('general.addProcesses.name', 'BPM', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.addProcesses.version', 'BPM', 'Version'),
                dataIndex: 'version',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.addProcesses.deploymentId', 'BPM', 'DeploymentID'),
                dataIndex: 'deploymentId',
                flex: 1
            }
        ]
    },
    buttonAlign: 'left',
    buttons: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.addProcesses.add','BPM','Add'),
            itemId: 'btn-add-processes',
            action: 'saveAddedProcesses',
            disabled: true,
            ui: 'action'
        },
        {
            xtype: 'button',
            itemId: 'btn-cancel-add-processes',
            text: Uni.I18n.translate('general.addProcesses.cancel','BPM','Cancel'),
            href: '#/administration/managementprocesses',
            ui: 'link'
        }
    ]

});

