/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.search.SearchSaveWindow
 */

Ext.define('Uni.view.search.SearchSaveWindow', {
    extend: "Ext.window.Window",
    xtype: 'uni-view-search-searchsavewindow',
    requires: [
        'Uni.store.search.SaveLoad'
    ],
    title:" ",
    layout: 'form',
    labelWidth: 120,
    required : true,
    bodyStyle: 'padding: 15px',
    resizable: false,
    draggable: false,
    modal: true,
    width: 580,
    height: 250,
    closeAction: 'close',
    align: 'center',
    style: 'border:1px solid green;border-radius: 8px;',
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
                    html: '<span class="icon-info" style="float : left;color: #70bb51; font-size: 35px; margin-right: 10px;"></span>'+ Uni.I18n.translate('general.saveCriteriaTitle', 'UNI', 'Save the search criteria'),
                    style: {
                        'font-size' : '25px',
                        'font-family': "'Open Sans Condensed', helvetica, arial, verdana, sans-serif",
                        'color': '#70bb51',
                        'margin-bottom': '10px',
                        'margin-right': '230px',
                        'font-weight': 'bold',
                    }
                }]},
        {
            xtype: 'container',
            required : true,
            layout: {
                type: 'hbox',
                pack: 'end',

            },
            items: [{
                xtype: 'combobox',
                id: 'saveEntered',
                style: {
                    'margin-right': '250px',
                    'margin-top': '20px'
                },
                emptyText:Uni.I18n.translate('general.typeName', 'UNI', 'Type a name'),
                fieldLabel:Uni.I18n.translate('general.nameCombo', 'UNI', 'Name') ,
                required: true,
                requiredField: true,
                allowBlank: false,
                forceSelect: true,
                typeAhead: true,
                minChars: 2,
                store: Ext.create('Uni.store.search.SaveLoad'),
                displayField: 'name',
                valueField: 'name',
                listConfig : {
                    maxHeight: 200,
                    style: "border-radius : 4px",
                    shadow: true,
                    bodyPadding: 10,
                    margin: 0
                }
            }]
        },
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                pack: 'end'
            },
            items: [{
                xtype: 'component',
                html: Uni.I18n.translate('general.overwriteIndication', 'UNI', 'The previously saved search criteria will be overwritten by entering the same name'),
                style: {
                    'font': 'italic 13px/17px Lato',
                    'color': '#686868',
                    'margin-top': '20px',
                    'margin-right': '10px'
                }
            }]
        }],
    buttons: [{
        text:  Uni.I18n.translate('general.save', 'UNI', 'Save'),
        type: 'button',
        ui: 'action',
        itemId: 'submit-search',
        action: 'saveSearch'
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