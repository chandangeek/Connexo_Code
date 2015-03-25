Ext.define('Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates', {
  extend: 'Ext.app.Controller',

  views: [
    'Dlc.devicelifecyclestates.view.Setup'
  ],

  stores: [
    'Dlc.devicelifecyclestates.store.DeviceLifeCycleStates'
  ],

  models: [
    'Dlc.devicelifecyclestates.model.DeviceLifeCycleState',
    'Dlc.devicelifecycles.model.DeviceLifeCycle'
  ],

  refs: [
    {
      ref: 'page',
      selector: 'device-life-cycle-states-setup'
    }
  ],

  init: function () {
    this.control({
      'device-life-cycle-states-setup device-life-cycle-states-grid': {
        select: this.showDeviceLifeCycleStatePreview
      }
    });
  },

  showDeviceLifeCycleStates: function () {
    var me = this,
    //todo : loading record
      lifecycleRecord = Ext.create('Dlc.devicelifecycles.model.DeviceLifeCycle', {name: 'Short device life cycle'}),
      view;

    view = Ext.widget('device-life-cycle-states-setup', {
      router: me.getController('Uni.controller.history.Router'),
      lifecycleRecord: lifecycleRecord
    });


    me.getApplication().fireEvent('changecontentevent', view);
    me.getApplication().fireEvent('devicelifecycleload', lifecycleRecord);
    view.down('device-life-cycle-states-grid').getSelectionModel().select(0);
      view.down('#device-life-cycle-link').setText(lifecycleRecord.get('name'));
  },

  showDeviceLifeCycleStatePreview: function (selectionModel, record, index) {
    var me = this,
      page = me.getPage(),
      preview = page.down('device-life-cycle-states-preview'),
      previewForm = page.down('device-life-cycle-states-preview-form');

    Ext.suspendLayouts();
    preview.setTitle(record.get('name'));
    previewForm.loadRecord(record);
    Ext.resumeLayouts(true);
  }
});