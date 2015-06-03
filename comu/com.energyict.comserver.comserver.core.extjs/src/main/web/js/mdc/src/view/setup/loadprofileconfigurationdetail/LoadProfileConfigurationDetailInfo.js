Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.loadProfileConfigurationDetailInfo',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Mdc.store.Intervals'
    ],
    width: '50%',
    defaults: {
        labelWidth: 200,
        labelAlign: 'right',
        validateOnChange: false,
        validateOnBlur: false,
        anchor: '100%'
    },
    items: [
        {
            xtype: 'displayfield',
            fieldLabel: 'Device configuration',
            htmlEncode: false,
            name: 'deviceConfigurationName'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Load profile type',
            name: 'name'
        },
        {
            xtype: 'obis-displayfield',
            name: 'obisCode'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Interval',
            name: 'timeDuration',
            renderer: function (value) {
                var intervalRecord = Ext.getStore('Mdc.store.Intervals').getById(value.id);
                return intervalRecord ? Ext.String.htmlEncode(intervalRecord.get('name')) : '';
            }
        }
    ],


    initComponent: function () {
        this.callParent(this);

    }
});

