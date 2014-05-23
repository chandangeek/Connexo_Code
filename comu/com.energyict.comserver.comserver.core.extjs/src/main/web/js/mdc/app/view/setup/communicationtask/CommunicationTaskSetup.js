Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.communicationTaskSetup',
    itemId: 'communicationTaskSetup',
    deviceTypeId: null,
    deviceConfigId: null,
    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            itemId: 'stepsContainer',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('communicationtasks.communicationtasks', 'MDC', 'Communication tasks') + '</h1>'
                },
                {
                    xtype: 'container',
                    itemId: 'CommunicationTaskDockedItems'
                },
                {
                    xtype: 'container',
                    itemId: 'CommunicationTaskEmptyList'
                },
                {
                    xtype: 'communicationTaskGrid'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'communicationTaskPreview'
                }
            ]}
    ],

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'deviceConfigurationMenu',
                itemId: 'stepsMenu',
                deviceTypeId: me.deviceTypeId,
                deviceConfigurationId: me.deviceConfigId,
                toggle: 7
            }
        ];
        me.callParent(arguments);
        me.down('#CommunicationTaskDockedItems').add(
            {
                xtype: 'communicationTaskDockedItems',
                deviceTypeId: me.deviceTypeId,
                deviceConfigurationId: me.deviceConfigId
            }
        );
    }
});


