Ext.define('Mdc.controller.setup.ChangeDeviceLifeCycle', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicetype.changedevicelifecycle.Browse'        
    ],

    stores: [
        'Mdc.store.DeviceLifeCycles'
    ],

    refs: [
        {
            ref: 'wizard',
            selector: 'change-device-life-cycle-browse change-device-life-cycle-wizard'
        },
        {
            ref: 'navigation',
            selector: 'change-device-life-cycle-browse change-device-life-cycle-navigation'
        }
    ],

    deviceType: null,

    init: function () {
        this.control({
            'change-device-life-cycle-browse change-device-life-cycle-wizard button[action=step-next]': {
                click: this.moveTo
            }
        });
    },

    showChangeDeviceLifeCycle: function (deviceTypeId) {
        var me = this,
            deviceTypeModel = me.getModel('Mdc.model.DeviceType'),
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('change-device-life-cycle-browse', {
                router: router
            });

        if (router.queryParams.previousRoute) {
            Uni.util.History.suspendEventsForNextCall();
            window.location.replace(router.getRoute().buildUrl(router.arguments, null));
        }
        me.getApplication().fireEvent('changecontentevent', view);
        view.setLoading();
        deviceTypeModel.load(deviceTypeId, {
            success: function (deviceType) {
                me.deviceType = deviceType;
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                view.down('#change-device-life-cycle-combo').getStore().load({
                    callback: function () {
                        view.down('#change-device-life-cycle-combo').getStore().filterBy(function(rec) {
                            return rec.get('id') != deviceType.get('deviceLifeCycleId');
                        });
                        view.setLoading(false);
                    }
                });
            }
        });
    },

    moveTo: function () {
        var me = this,
            wizard = me.getWizard(),
            router = me.getController('Uni.controller.history.Router'),
            nextBtn = wizard.down('#change-device-life-cycle-next'),
            backBtn = wizard.down('#change-device-life-cycle-step-back'),
            cancelBtn = wizard.down('#change-device-life-cycle-cancel'),
            finishBtn = wizard.down('#change-device-life-cycle-finish'),
            lifeCycleCombo = wizard.down('#change-device-life-cycle-combo');

        lifeCycleCombo.clearInvalid();
        wizard.down('#form-errors').hide();
        wizard.setLoading();
        Ext.Ajax.suspendEvent('requestexception');
        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/{id}/devicelifecycle'.replace('{id}', router.arguments.deviceTypeId),
            method: 'PUT',
            jsonData: {
                version: me.deviceType.get('version'),
                targetDeviceLifeCycle: {
                    id: lifeCycleCombo.getValue()
                }
            },
            callback: function (options, success, response) {
                wizard.setLoading(false);
                var result = Ext.decode(response.responseText, true);
                if (lifeCycleCombo.getValue()) {
                    nextBtn.hide();
                    backBtn.hide();
                    cancelBtn.hide();
                    finishBtn.show();
                    wizard.getLayout().setActiveItem(1);
                    me.getNavigation().moveToStep(2);
                    wizard.down('change-device-life-cycle-step2').setResultMessage(result, success);
                } else {
                    lifeCycleCombo.markInvalid(result.errors[0].msg);
                    wizard.down('#form-errors').show();
                }
                Ext.Ajax.resumeEvent('requestexception');
            }
        });
    }
});
