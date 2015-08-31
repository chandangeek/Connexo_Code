Ext.define('Dlc.devicelifecyclestates.store.AvailableTransitionBusinessProcesses', {
    extend: 'Ext.data.Store',
    requires: [
        'Dlc.devicelifecyclestates.model.TransitionBusinessProcess'
    ],
    model: 'Dlc.devicelifecyclestates.model.TransitionBusinessProcess',
    storeId: 'AvailableTransitionBusinessProcesses',
    remoteFilter: false,
    sorters: [{
        property: 'processId',
        direction: 'ASC'
    }],
/*
    // for 'playing' purposes
    proxy: {
        type: 'memory'
    },
    data: [
         {id: 1, deploymentId: 'com.elster.mail.send', processId:'send mail'},
         {id: 2, deploymentId: 'com.elster.print.installationAcceptance', processId: 'print installation acceptance'},
         {id: 3, deploymentId: 'com.elster.print.defectReport', processId: 'print defect report'}
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