Ext.define('Mdc.filemanagement.controller.FileManagement', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.filemanagement.view.Setup'
    ],

    stores: [],

    models: [
        'Mdc.model.DeviceType'
    ],

    refs: [],

    deviceTypeId: null,
    init: function () {
        var me = this;

        me.control({});
    },

    showFileManagementOverview: function (deviceTypeId) {
        var me = this,
            view;
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('device-type-files-setup', {
                    deviceTypeId: deviceTypeId
                });
                me.deviceTypeId = deviceTypeId;
                me.getApplication().fireEvent('changecontentevent', view);
                view.suspendLayouts();
                view.setLoading(true);
                me.reconfigureMenu(deviceType, view);
            }
        });
    },

    reconfigureMenu: function (deviceType, view) {
        var me = this;
        me.getApplication().fireEvent('loadDeviceType', deviceType);
        if (view.down('deviceTypeSideMenu')) {
            view.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
            view.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
            );
        }

        view.setLoading(false);
        view.resumeLayouts();

    }

});