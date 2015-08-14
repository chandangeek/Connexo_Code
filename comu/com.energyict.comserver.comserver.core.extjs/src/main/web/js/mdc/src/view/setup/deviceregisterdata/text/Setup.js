Ext.define('Mdc.view.setup.deviceregisterdata.text.Setup', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainSetup',
    alias: 'widget.deviceregisterreportsetup-text',
    itemId: 'deviceregisterreportsetup',

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'deviceregisterreportgrid-text',
                            mRID: me.mRID,
                            registerId: me.registerId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-register-data',
                            title: Uni.I18n.translate('device.registerData.noData', 'MDC', 'No readings found'),
                            reasons: [
                                Uni.I18n.translate('device.registerData.list.item1', 'MDC', 'No readings have been defined yet.'),
                                Uni.I18n.translate('device.registerData.list.item2', 'MDC', 'No readings comply to the filter.')
                            ],
                            stepItems: [
                                {
                                    text:  Uni.I18n.translate('general.addReading','MDC','Add reading'),
                                    privileges: Mdc.privileges.Device.administrateDeviceData,
                                    href: '#/devices/' + encodeURIComponent(me.mRID) + '/registers/' + me.registerId + '/data/add',
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceregisterreportpreview-text'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});