/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

var profile = {
    resourceTags: {
        ignore: function(filename, mid){
            // only include moment/moment
            return mid != "moment/moment";
        },
        amd: function(filename, mid){
            return /\.js$/.test(filename);
        }
    }
};
