/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.search.RemoveSaveWindow
 */

Ext.define('Uni.view.search.RemoveSaveWindow', {
    extend: "Ext.window.Window",
    xtype: 'uni-view-search-removesavewindow',
    requires: [
        'Uni.store.search.SaveLoad'
    ],
    style: 'border-radius: 8px; border:0.5px solid #eb5642',
    layout: 'form',
    labelWidth: 120,
    required : true,
    bodyStyle: 'padding: 15px',
    resizable: false,
    draggable: false,
    modal: true,
    orange: true,
    width: 450,
    height: 200,
    closeAction: 'close',
    align: 'center',
    title : " ",
    items: [
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                pack: 'end'
            },
            items: [
                {
                    xtype: 'component',
                    align : 'center',
                    html: '<span class="icon-warning" style="float : left;color: #eb5642; font-size: 35px; margin-right: 10px;"></span>'+ Uni.I18n.translate('general.removeSave', 'UNI', 'Remove?'),
                    style: {
                        'font-size' : '25px',
                        'font-family': "'Open Sans Condensed', helvetica, arial, verdana, sans-serif",
                        'color': '#eb5642',
                        'margin-bottom': '10px',
                        'margin-right': '230px',
                        'font-weight': 'bold',
                    }
                }]},
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                pack: 'end'
            },
            items: [
                {
                    xtype: 'component',
                    align : 'center',
                    html: Uni.I18n.translate('general.removeIndication', 'UNI', 'The saved search criteria will be removed'),
                    style: {
                        'font': '15px/17px Lato',
                        'color': '#686868',
                        'margin-top': '15px',
                        'margin-right': '70px',

                    }
                }]}
    ],
    buttons: [{
        text:  Uni.I18n.translate('general.remove', 'UNI', 'Remove'),
        type: 'button',
        ui: 'remove',
        itemId: 'remove-search',
        action: 'removeSearch'
    },
        {
            text:  Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
            ui: 'link',
            handler: function () {
                this.up('window').destroy()
            }
        }],
    buttonAlign: 'right'

});