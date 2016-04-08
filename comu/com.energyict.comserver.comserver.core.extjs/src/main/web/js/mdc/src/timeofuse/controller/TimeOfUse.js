Ext.define('Mdc.timeofuse.controller.TimeOfUse', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.timeofuse.view.Setup',
    ],

    stores: [],

    models: ['Mdc.model.DeviceType'],

    refs: [],

    showTimeOfUseOverview: function (deviceTypeId) {
        var me = this,
            view = Ext.widget('device-type-tou-setup', {
                deviceTypeId: deviceTypeId
            });

        me.getApplication().fireEvent('changecontentevent', view);
        view.setLoading(true);
        view.suspendLayouts();
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                if (view.down('deviceTypeSideMenu')) {
                    view.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
                    view.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                        Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
                    );
                }

                view.setLoading(false);
                view.resumeLayouts();
            },
            failure: function () {
                view.setLoading(false);
                view.resumeLayouts();
            }
        });
    }


});