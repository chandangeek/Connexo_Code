/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.controller.EventType', {
    extend: 'Ext.app.Controller',

    requires: [
        'Cfg.view.eventtype.Browse'
    ],

    stores: [
        'EventTypes'
    ],

    views: [
        'eventtype.Browse'
    ],

    init: function () {
        this.initMenu();

        this.control({
            'eventtypeList button[action=save]': {
                click: this.saveEventTypes
            }
        });
    },

    saveEventTypes: function (button) {
        this.getEventTypesStore().sync({
            success: this.saveSuccess,
            failure: this.saveFailed
        });
    },

    saveSuccess: function () {
        alert('Saved');
    },

    saveFailed: function () {
        alert('Failed');
    },

    initMenu: function () {
//        var me = this;
//        var menuItem = Ext.create('Uni.model.MenuItem', {
//            text: 'EventTypes',
//            href: me.getApplication().getHistoryEventTypeController().tokenizeShowOverview(),
//            glyph: 'xe01e@icomoon'
//        });
//
//        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        this.getEventTypesStore().load();

        var widget = Ext.widget('eventtypeBrowse');
        this.getApplication().getController('Cfg.controller.Main').showContent(widget);
    }

});
