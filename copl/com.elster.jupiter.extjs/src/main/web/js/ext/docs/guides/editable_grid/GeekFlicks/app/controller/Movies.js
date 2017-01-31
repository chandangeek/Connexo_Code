/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define("GeekFlicks.controller.Movies", {
    extend: 'Ext.app.Controller',
    views: ['Movies'],
    models: ['Movie'],
    stores: ['Movies'],
    init: function () {
        this.control({
            '#movies_editor': {
                render: this.onEditorRender
            }
        });
    },

    onEditorRender: function () {
        console.log("movies editor was rendered");
    }
});
