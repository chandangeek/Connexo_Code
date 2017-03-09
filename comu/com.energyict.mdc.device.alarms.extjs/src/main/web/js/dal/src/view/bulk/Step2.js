Ext.define('Dal.view.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.alarm-bulk-step2',
    title: Uni.I18n.translate('alarm.selectAction','DAL','Select action'),

    requires: [
        'Ext.form.RadioGroup'
    ],

    initComponent: function () {
        var me = this,
            icon = '<span class="uni-icon-info-small" style="cursor: pointer;display: inline-block;width: 16px;height: 16px;margin-left: 5px;float: none;vertical-align: bottom;" data-qtip="' +
                Uni.I18n.translate('general.helpTooltip', 'DAL', 'Click for more information') + '"></span>';

        me.items = [
            {
                xtype: 'panel',
                border: false,
                items: [
                    {
                        itemId: 'radiogroupStep2',
                        xtype: 'radiogroup',
                        columns: 1,
                        vertical: true,
                        defaults: {
                            name: 'operation',
                            submitValue: false
                        },
                        items: [
                            {
                                itemId: 'Assign',
                                boxLabel: Uni.I18n.translate('alarms.assignAlarms','DAL','Assign alarms'),
                                name: 'operation',
                                inputValue: 'assign',
                                checked: true,
                                privileges: Dal.privileges.Alarm.assign
                            },
                            {
                                itemId: 'Close',
                                boxLabel: Uni.I18n.translate('alarms.closeAlarms', 'DAL', 'Close alarms'),
                                name: 'operation',
                                inputValue: 'close',
                                privileges: Dal.privileges.Alarm.close
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

});