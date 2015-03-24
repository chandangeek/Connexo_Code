Ext.define('Dlc.devicelifecyclestates.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-life-cycle-states-preview-form',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                name: 'name',
                labelWidth: 250
            },
          {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('deviceLifeCycleStates.processesOnEntry', 'DLC', 'Processes on entry'),
            name: 'entry',
            labelWidth: 250
          },
          {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('deviceLifeCycleStates.processesOnExit', 'DLC', 'Processes on exit'),
            name: 'exit',
            labelWidth: 250
          }
        ];

        me.callParent(arguments);
    }
});
