Ext.define('Mdc.view.setup.deviceregisterdata.MainPreview', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Mdc.view.setup.deviceregisterdata.ActionMenu',
        'Uni.form.field.EditedDisplay'
    ],

    frame: true,

    mentionDataLoggerSlave: false,
    router: null,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            privileges: Mdc.privileges.Device.administrateDeviceData,
            iconCls: 'x-uni-action-iconD',
            itemId: 'gridPreviewActionMenu',
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions,
            menu: {
                xtype: 'deviceregisterdataactionmenu'
            }
        }
    ],

    initComponent: function () {
        var me = this;
        if (me.mentionDataLoggerSlave) {
            me.on('afterrender', function() {
                me.down('#mdc-register-data-preview-fields-container').insert(1,
                    {
                        xtype: 'displayfield',
                        labelWidth: 200,
                        fieldLabel: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
                        itemId: 'mdc-register-data-preview-data-logger-slave',
                        name: 'slaveRegister',
                        renderer: function() {
                            var record = this.up('form').getRecord(),
                                slaveRegister = record ? record.get('slaveRegister') : undefined;
                            if (Ext.isEmpty(slaveRegister)) {
                                return '-';
                            }
                            var slaveMRID = slaveRegister.mrid,
                                registerId = slaveRegister.registerId;
                            return Ext.String.format('<a href="{0}">{1}</a>',
                                me.router.getRoute('devices/device/registers/registerdata').buildUrl(
                                    {
                                        mRID: encodeURIComponent(slaveMRID),
                                        registerId: registerId
                                    },
                                    me.router.queryParams
                                ),
                                slaveMRID);
                        }
                    }
                );
            });
        }
        me.callParent(arguments);
    }
});


