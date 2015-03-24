Ext.define('Dlc.devicelifecyclestates.store.DeviceLifeCycleStates', {
    extend: 'Ext.data.Store',
    model: 'Dlc.devicelifecyclestates.model.DeviceLifeCycleState',
    data: [
      {id: 1, name: 'Commisioned', entry: '-', exit: '-'},
      {id: 2, name: 'Decommisioned', entry: '-', exit: '-'},
      {id: 3, name: 'Deleted', entry: '-', exit: '-'},
      {id: 4, name: 'In stock', entry: '-', exit: '-'},
      {id: 5, name: 'Installed and active', entry: '-', exit: '-'},
      {id: 5, name: 'Installed and inactive', entry: '-', exit: '-'}
    ]
});