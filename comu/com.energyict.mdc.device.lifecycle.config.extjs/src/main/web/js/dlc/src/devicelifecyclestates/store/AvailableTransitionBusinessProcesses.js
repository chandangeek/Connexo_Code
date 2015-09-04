Ext.define('Dlc.devicelifecyclestates.store.AvailableTransitionBusinessProcesses', {
    extend: 'Ext.data.Store',
    requires: [
        'Dlc.devicelifecyclestates.model.TransitionBusinessProcess'
    ],
    model: 'Dlc.devicelifecyclestates.model.TransitionBusinessProcess',
    storeId: 'AvailableTransitionBusinessProcesses',
    remoteFilter: false,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
/*
    // for 'playing' purposes
    proxy: {
        type: 'memory'
    },
    data: [
         {id: 1, name: 'send mail', deploymentId: 'com.elster.mail.send', processId:'sendMail'},
         {id: 2, name: 'print installation acceptance', 'com.elster.print.installationAcceptance', processId: 'printInstallationAcceptance'},
         {id: 3, name: 'print defect report', deploymentId: 'com.elster.print.defectReport', processId: 'printDefectReport'}
    ]
*/

    proxy: {
        type: 'rest',
        url: '/api/dld/devicelifecycles/statechangebusinessprocesses',
        reader: {
            type: 'json',
            root: 'stateChangeBusinessProcesses'
        },
        startParam: undefined,
        limitParam: undefined,
        pageParam: undefined
    }
});