/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ext.calendar.data.Calendars', {
    statics: {
        getData: function(){
            return {
                "calendars":[{
                    "id":    1,
                    "title": "Home"
                },{
                    "id":    2,
                    "title": "Work"
                },{
                    "id":    3,
                    "title": "School"
                }]
            };    
        }
    }
});