/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.systemprops.SystemProps', {
    extend: 'Ext.app.Controller',

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
        var record = form.getRecord();
        var page = me.getPage();
        record.getProxy().url = '/api/sp/systemproperties';
        record.phantom = false; //Needed to send PUT request instead of POST on save call.
        record.getProxy().appendId = false; //To avoid adding id to request url.

        record.save({
            isNotEdit: true,
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.sysPropsUpdateSuccess', 'SAM', 'System properties saved'));
            },
            failure: function (record, response) {
                var responseText = Ext.decode(response.response.responseText, true);

                if (responseText && Ext.isArray(responseText.errors)) {
                    form.markInvalid(responseText.errors);
                }
            },
            callback: function () {
                me.getPage().down('#sys-prop-attributes-actions-button').setDisabled(false);
                page.setLoading(false);
            }
        });
    },

    editAttributes: function (form) {
        var me = this;
        var menu = me.getPage().down('#sys-prop-attributes-actions-menu');
        menu.down('#edit-system-properties').hide();
        form.switchDisplayMode('edit');
        me.getPage().down('#sys-prop-attributes-actions-button').setDisabled(true);
    },

    cancelEditAttributes: function (form) {
        var me = this;
        var menu = me.getPage().down('#sys-prop-attributes-actions-menu');
        menu.down('#edit-system-properties').show();
        form.switchDisplayMode('view');
        me.getPage().down('#sys-prop-attributes-actions-button').setDisabled(false);
    },

    showSystemProps: function () {
        var me = this;

        var widget = Ext.widget('system-props-container', {
                itemId: 'system-props-container'
        });
        this.getApplication().fireEvent('changecontentevent', widget);

         me.getModel('Sam.model.SystemPropsInfo').load(null, {
                            success: function (record) {
                                me.getPage().loadRecord(record);
                            }
         });
    },

    chooseAction: function (menu, item) {
            var me = this;
            var menu = me.getPage().down('#sys-prop-attributes-actions-menu');
            menu.down('#edit-system-properties').hide();
            me.getPage().down('system-props-view').switchDisplayMode('edit');
            me.getPage().down('#sys-prop-attributes-actions-button').setDisabled(true);
    },
});