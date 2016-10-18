Ext.define('Imt.usagepointmanagement.controller.Calendars', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [

    ],

    models: [

    ],

    views: [
        'Imt.usagepointmanagement.view.calendars.Details'
        //'Uni.view.window.Confirmation'
    ],

    refs: [
        //{
        //    ref: 'page',
        //    selector: '#usage-point-attributes'
        //}
    ],

    init: function () {
        var me = this;

        //me.control({
        //    '#usage-point-attributes view-edit-form': {
        //        save: me.saveAttributes,
        //        edit: me.editAttributes,
        //        canceledit: me.cancelEditAttributes
        //    },
        //    '#usage-point-attributes #usage-point-attributes-actions-menu': {
        //        click: me.chooseAction
        //    }
        //});
    },

    showCalendars: function(mRID){
        var me = this,
            resultSet,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        usagePointsController.loadUsagePoint(mRID, {
            success: function (types, usagePoint) {
                me.usagePoint = usagePoint;
                me.getApplication().fireEvent('changecontentevent', Ext.widget('usage-point-calendar-configuration-details', {
                   // itemId: 'usage-point-metrology-configuration-details',
                    router: router,
                    usagePoint: usagePoint,
                   // meterRolesAvailable: usagePoint.get('metrologyConfiguration_meterRoles')
                }));

            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },

    addCalendar: function(){
        debugger;
    }



});