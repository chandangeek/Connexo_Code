/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.systemprops.SystemProps', {
    extend: 'Ext.app.Controller',

    stores: [
        'Sam.store.SystemInfo'
    ],

    views: [
        'Sam.view.systemprops.SysPropsContainer'
    ],

    models: [
        'Sam.model.SystemPropsInfo'
    ],


    refs: [
        {
            ref: 'page',
            selector: 'system-props-container'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#system-props-container system-props-view': {
                save: me.saveAttributes,
                edit: me.editAttributes,
                canceledit: me.cancelEditAttributes
            },
            '#system-props-container #sys-prop-attributes-actions-menu': {
                click: me.chooseAction
            }
        });
    },

    saveAttributes: function (form) {
        var me = this;
        console.log("SAVE ACTION!!!!!!!!!!!!!");
        var record = form.getRecord();
        var page = me.getPage();
        var propInfo = me.getPage().propertyInfo;
        console.log("OBTAINED RECORD="+record);
        console.log("OBTAINED RECORD PROPERTIES="+record.properties());
        record.properties().each(function (property) {
            console.log("property ="+property);
        });
        propInfo.set('properties', record.properties());
        console.log("propInfo="+propInfo);
        console.log("propInfo.properties="+propInfo.properties());
        propInfo.properties().each(function (property) {
                    console.log("property to send="+property);
                });
        //propInfo.save();
        record.getProxy().url = '/api/sp/systemproperties';
        record.phantom = false; //Needed to send PUT request instead of POST on save call.
        record.getProxy().appendId = false; //To avoid adding id to request url.
        //record.id = 1;
        console.log("SAAAAAAAAAAAAVE!!!!!!!!!!");
        record.save({
            isNotEdit: true,
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute().forward();
                me.getApplication().fireEvent('acknowledge', 'OKEEEE'/*Uni.I18n.translate('usagePoint.acknowledge.updateSuccess', 'IMT', 'Usage point saved')*/);
            },
            failure: function (record, response) {
                var responseText = Ext.decode(response.response.responseText, true);

                if (responseText && Ext.isArray(responseText.errors)) {
                    form.markInvalid(responseText.errors);
                }
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    editAttributes: function (form) {
        var me = this;
        console.log("EDIT ACTION!!!!!!!!!!!!!");
        var menu = me.getPage().down('#sys-prop-attributes-actions-menu');

        menu.down('#edit-system-properties').hide();
        form.switchDisplayMode('edit');
    },

    cancelEditAttributes: function (form) {
        var me = this;
        console.log("CANCEL ACTION!!!!!!!!!!!!!");
        var menu = me.getPage().down('#sys-prop-attributes-actions-menu');
        menu.down('#edit-system-properties').show();
        form.switchDisplayMode('view');
    },

    showSystemProps: function () {
        var me = this;
        console.log("SHOW SYSTEM PROPS!!!!!");

        //var widget = Ext.widget('system-props-view');
        var widget = Ext.widget('system-props-container', {
                itemId: 'system-props-container'/*,
                router: router,
                usagePoint: usagePoint*/
        });
        this.getApplication().fireEvent('changecontentevent', widget);

        //var propertyForm = me.getPage().down('property-form');

        console.log("GET MODEL AND LOAD!!!!!");
         me.getModel('Sam.model.SystemPropsInfo').load(null, {
                            success: function (record) {
                                console.log("RECORD ="+record);
                                console.log("RECORD.propeties ="+record.properties());
                                var propeses = record.properties()
                               propeses.each(function (property) {
                               console.log("property ="+property);
                               });

                                me.getPage().loadRecord(record);
                            }
         });
    },

    chooseAction: function (menu, item) {
            var me = this;

            console.log("CHOOOSE ACTION!!!!!!!");
            var menu = me.getPage().down('#sys-prop-attributes-actions-menu');
            menu.down('#edit-system-properties').hide();
            me.getPage().down('system-props-view').switchDisplayMode('edit');

    },
});