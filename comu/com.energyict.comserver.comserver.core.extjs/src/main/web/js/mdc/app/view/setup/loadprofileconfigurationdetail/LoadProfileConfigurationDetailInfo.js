Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileConfigurationDetailInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.loadProfileConfigurationDetailInfo',
    itemId: 'loadProfileConfigurationDetailInfo',
    intervalStore: null,

    width: '50%',
    defaults: {
        labelWidth: 150,
        labelAlign: 'right',
        margin: '0 0 20 0',
        validateOnChange: false,
        validateOnBlur: false,
        anchor: '100%'
    },
    items: [
        {
            xtype: 'displayfield',
            fieldLabel: 'Device configuration: ',
            name: 'deviceConfigurationName'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Load profile configuration: ',
            itemId: 'loadProfileConfigLink'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Load profile type: ',
            name: 'name'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'OBIS code: ',
            name: 'obisCode'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Interval: ',
            name: 'timeDuration',
            renderer: function (value) {
                var intervalRecord = this.up('#loadProfileConfigurationDetailInfo').intervalStore.findRecord('id', value.id);
                if (!Ext.isEmpty(intervalRecord)) {
                    return intervalRecord.getData().name;
                }
            }
        }
    ],


    initComponent: function () {
        this.callParent(this);

    }
});

