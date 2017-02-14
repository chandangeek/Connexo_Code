/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.geo.SpatialCoordinatesFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class AddLocationInfoToDevicesCommand {
    private final DataModel dataModel;
    private final DeviceService deviceService;
    private final MeteringService meteringService;

    private static final List<String> administrativeAreaList = Arrays.asList("Ohio,Massachusetts,Tennessee,California,Maryland,Florida,Florida,California,California,California,Texas,Texas,Pennsylvania,Washington,Texas,South Dakota,California,Indiana,Louisiana,North Carolina,Washington,California,Hawaii,Oklahoma,Tennessee,Georgia,Florida,West Virginia,Nevada,California,New York,Colorado,Pennsylvania,Ohio,Texas,Texas,Iowa,Florida,Georgia,Texas,Missouri,Pennsylvania,Michigan,Utah,Minnesota,California,Hawaii,Georgia,Tennessee,Nevada,Florida,Georgia,California,Nevada,Indiana,Wisconsin,California,Alabama,Georgia,Colorado,Pennsylvania,Utah,New York,Florida,Texas,Florida,New York,Missouri,Georgia,Indiana,Minnesota,Florida,Ohio,Colorado,District of Columbia,Kentucky,Virginia,Virginia,New York,District of Columbia,Texas,Minnesota,Louisiana,Nevada,Arizona,Nevada,New York,Louisiana,North Carolina,California,Colorado,California,South Carolina,Alabama,Florida,Virginia,Alabama,California,Hawaii"
            .split(","));
    private static final List<String> localityList = Arrays.asList("Hamilton,Boston,Johnson City,Pomona,Baltimore,Pensacola,Pensacola,Whittier,Riverside,Irvine,Beaumont,Houston,Philadelphia,Vancouver,Houston,Sioux Falls,Oakland,South Bend,New Orleans,Charlotte,Seattle,San Jose,Honolulu,Tulsa,Nashville,Atlanta,Jacksonville,Charleston,Las Vegas,Los Angeles,New York City,Colorado Springs,Pittsburgh,Cincinnati,Fort Worth,El Paso,Des Moines,Miami,Macon,Dallas,Springfield,Valley Forge,Flint,Ogden,Minneapolis,Sacramento,Honolulu,Augusta,Memphis,Reno,Panama City,Athens,San Diego,North Las Vegas,Indianapolis,Green Bay,Sacramento,Birmingham,Atlanta,Denver,Philadelphia,Ogden,New York City,West Palm Beach,Houston,Vero Beach,Brooklyn,Kansas City,Columbus,Evansville,Saint Paul,West Palm Beach,Dayton,Arvada,Washington,Lexington,Merrifield,Norfolk,Syracuse,Washington,Fort Worth,Minneapolis,Baton Rouge,Las Vegas,Phoenix,North Las Vegas,Jamaica,New Orleans,Raleigh,San Diego,Greeley,Glendale,Spartanburg,Huntsville,Gainesville,Richmond,Birmingham,Santa Clara,Honolulu"
            .split(","));
    private static final List<String> subLocalityList = Arrays.asList("OH,MA,TN,CA,MD,FL,FL,CA,CA,CA,TX,TX,PA,WA,TX,SD,CA,IN,LA,NC,WA,CA,HI,OK,TN,GA,FL,WV,NV,CA,NY,CO,PA,OH,TX,TX,IA,FL,GA,TX,MO,PA,MI,UT,MN,CA,HI,GA,TN,NV,FL,GA,CA,NV,IN,WI,CA,AL,GA,CO,PA,UT,NY,FL,TX,FL,NY,MO,GA,IN,MN,FL,OH,CO,DC,KY,VA,VA,NY,DC,TX,MN,LA,NV,AZ,NV,NY,LA,NC,CA,CO,CA,SC,AL,FL,VA,AL,CA,HI"
            .split(","));
    private static final List<String> streetTypeList = Arrays.asList("Plaza,Drive,Avenue,Pass,Circle,Parkway,Junction,Trail,Drive,Lane,Trail,Street,Lane,Way,Point,Circle,Way,Avenue,Point,Drive,Drive,Avenue,Avenue,Place,Place,Way,Crossing,Road,Road,Drive,Court,Parkway,Junction,Hill,Crossing,Point,Place,Alley,Circle,Plaza,Avenue,Plaza,Crossing,Crossing,Circle,Pass,Terrace,Avenue,Place,Terrace,Parkway,Court,Junction,Parkway,Court,Plaza,Drive,Center,Place,Park,Drive,Place,Avenue,Alley,Trail,Point,Center,Hill,Place,Pass,Alley,Avenue,Center,Hill,Avenue,Park,Center,Lane,Place,Plaza,Circle,Park,Avenue,Hill,Alley,Hill,Terrace,Court,Junction,Junction,Pass,Lane,Drive,Terrace,Pass,Trail,Pass,Court,Avenue"
            .split(","));
    private static final List<String> streetNameList = Arrays.asList("Cody,Cardinal,Burrows,Garrison,Eliot,Algoma,Myrtle,Eastwood,Dakota,Kinsman,Menomonie,Cherokee,Calypso,Merrick,Fisk,Drewry,Portage,Lien,Oak Valley,Golden Leaf,Stephen,Express,Bay,Morning,Meadow Vale,Evergreen,Daystar,Kedzie,Glendale,Dahle,Arapahoe,Harbort,Judy,Melody,6th,Susan,Sundown,Daystar,Nelson,Saint Paul,Kings,Oak Valley,Scoville,John Wall,Spohn,Schurz,Westend,Hintze,Algoma,Eagle Crest,Walton,Graceland,Mosinee,Hudson,Eastlawn,Mallard,Porter,Dunning,Heath,Harbort,Grasskamp,Oakridge,Tony,Maple Wood,Hintze,Carioca,Meadow Valley,Stang,Mcbride,Cherokee,Birchwood,Harper,Bultman,Texas,Northfield,Miller,Roth,Northwestern,Texas,Sommers,Haas,Lakewood,Fordem,Oak,Wayridge,Anzinger,Jana,Waywood,Moose,Riverside,Washington,Mayfield,Harbort,Express,Killdeer,Judy,Tomscot,1st,Declaration"
            .split(","));
    private static final List<String> genericNumberList = Arrays.asList("1,9026,9,83,9897,5777,483,61989,4,9328,5,341,78799,496,54675,1509,93,20,5039,772,6507,205,4,4025,619,4,2,864,74358,1701,3485,34,78,75,676,823,5,92369,3564,58294,28,349,61278,80887,8,54,58,26,3,4,3,26816,821,41208,34439,6,3184,5986,57,81759,109,38,18,3977,43,3188,4684,30,3412,2358,738,8288,9,94171,4953,927,11,56,8541,99,98280,332,12,3174,5,79640,11,537,492,1736,6375,2984,1,94,4,8611,81,58,8257"
            .split(","));
    private static final List<String> establishmentTypeList = Arrays.asList("Block", "House", "Duplex", "Office Building");
    private static final List<String> establishmentNameList = Arrays.asList("Edgeify,Riffpath,Riffpedia,Mynte,Bubblemix,Buzzbean,Skyba,Avavee,Chatterpoint,Katz,Yodel,Lajo,Twimbo,Riffpedia,Skiba,Devpoint,Roomm,Vinder,Gigazoom,Topicstorm,Quire,Skyba,Jabbersphere,Centizu,Realfire,Skyba,Vitz,Flashpoint,Devpulse,Gigabox,Yakidoo,Feedfire,Skibox,Yakijo,Meembee,Jaloo,Tagopia,Trupe,Twinder,Devshare,Mycat,Dynava,Twimm,Babbleopia,Zooxo,Flipstorm,Dynava,Buzzdog,Flashset,Jabbercube,Jabbercube,Realfire,Edgeblab,Rhycero,Quinu,Youtags,Dablist,Quatz,Avaveo,Skimia,Trudoo,Minyx,Jabbersphere,Riffpath,Thoughtworks,Gabtune,Buzzster,Wordify,Vinder,Fivespan,Divape,Dynava,Quimm,Jaxspan,Avavee,Layo,Dabjam,Quinu,Roomm,Skiba,Shuffledrive,Latz,Realpoint,Avamba,Gigaclub,Devcast,Quimm,Tazzy,Rhybox,Bubbletube,Flipstorm,Skimia,Centizu,Gigaclub,Twimm,Rhyloo,Divape,Aivee,Yodoo"
            .split(","));
    private static final List<String> addressDetailList = Arrays.asList("reddit.com,parallels.com,myspace.com,e-recht24.de,imgur.com,rediff.com,ifeng.com,joomla.org,unblog.fr,harvard.edu,dot.gov,bravesites.com,squarespace.com,w3.org,ask.com,dailymail.co.uk,illinois.edu,acquirethisname.com,paypal.com,lycos.com,istockphoto.com,apache.org,marketwatch.com,dagondesign.com,msu.edu,hao123.com,chronoengine.com,skype.com,chicagotribune.com,sciencedaily.com,com.com,cornell.edu,homestead.com,vistaprint.com,shutterfly.com,cbsnews.com,hao123.com,ted.com,joomla.org,ft.com,wikipedia.org,adobe.com,networksolutions.com,sohu.com,google.co.uk,spiegel.de,ezinearticles.com,wisc.edu,simplemachines.org,timesonline.co.uk,naver.com,ezinearticles.com,whitehouse.gov,example.com,webnode.com,histats.com,ocn.ne.jp,joomla.org,rakuten.co.jp,parallels.com,upenn.edu,ow.ly,indiatimes.com,fema.gov,dyndns.org,is.gd,symantec.com,so-net.ne.jp,imageshack.us,yellowpages.com,rakuten.co.jp,wix.com,wikipedia.org,bluehost.com,google.it,last.fm,earthlink.net,i2i.jp,shop-pro.jp,ocn.ne.jp,boston.com,unicef.org,hubpages.com,theguardian.com,google.ca,typepad.com,amazon.co.uk,china.com.cn,cnn.com,mapquest.com,zimbio.com,friendfeed.com,nasa.gov,msn.com,mapquest.com,twitter.com,hubpages.com,fc2.com,xrea.com"
            .split(","));
    private static final List<String> zipCodeList = Arrays.asList("45020,2104,37605,91797,21216,32595,32526,90605,92513,92710,77713,77075,19136,98664,77212,57188,94660,46634,70183,28272,98104,95108,96820,74116,37205,30316,32204,25305,89178,90094,10120,80940,15286,45213,76192,88535,50330,33185,31296,75221,65805,19495,48555,84409,55480,94291,96845,30919,38150,89550,32405,30610,92145,89036,46278,54305,95894,35254,31106,80241,19160,84409,10170,33421,77281,32964,11236,64101,31904,47747,55146,33421,45414,80005,20540,40591,22119,23514,13224,20005,76192,55428,70805,89130,85053,89036,11470,70160,27635,92105,80638,91205,29305,35895,32610,23237,35285,95054,96805"
            .split(","));

    private List<Device> devices;

    @Inject
    public AddLocationInfoToDevicesCommand(DataModel dataModel, DeviceService deviceService, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.deviceService = deviceService;
        this.meteringService = meteringService;
    }

    public AddLocationInfoToDevicesCommand setDevices(List<Device> devices) {
        this.devices = Collections.unmodifiableList(devices);
        return this;
    }

    public void run() {
        getDeviceList().forEach(device -> {
            if (!this.dataModel.getSqlDialect().name().equalsIgnoreCase("H2")) {
                device.setSpatialCoordinates(createSpatialCoordinates());
            }
            device.setLocation(createLocation());
            device.save();
        });
    }

    private List<Device> getDeviceList() {
        return this.devices != null ? this.devices : this.deviceService.deviceQuery().select(where("name").like(Constants.Device.STANDARD_PREFIX + "*"));
    }

    private SpatialCoordinates createSpatialCoordinates() {
        double minLatitude = -90.00;
        double maxLatitude = 90.00;
        double minLongitude = -180.00;
        double maxLongitude = 180.00;
        DecimalFormat df = new DecimalFormat("#.#####");
        String latitude = df.format(minLatitude + Math.random() * (maxLatitude - minLatitude));
        String longitude = df.format(minLongitude + Math.random() * (maxLongitude - minLongitude));
        String elevation = String.valueOf((int) (Math.random() * 10));
        return new SpatialCoordinatesFactory().fromStringValue(latitude + ":" + longitude + ":" + elevation);
    }

    private Location createLocation() {
        LocationBuilder builder = this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get().newMeter(String.valueOf(KnownAmrSystem.MDC.getId()), "Fake").newLocationBuilder();
        setLocationAttributes(builder.member()).add();
        return builder.create();
    }

    private LocationBuilder.LocationMemberBuilder setLocationAttributes(LocationBuilder.LocationMemberBuilder builder) {
        builder.setCountryCode("US")
                .setCountryName("United States")
                .setAdministrativeArea(administrativeAreaList.get((int) (Math.random() * (administrativeAreaList.size() - 1))))
                .setLocality(localityList.get((int) (Math.random() * (localityList.size() - 1))))
                .setSubLocality(subLocalityList.get((int) (Math.random() * (subLocalityList.size() - 1))))
                .setStreetType(streetTypeList.get((int) (Math.random() * (streetTypeList.size() - 1))))
                .setStreetName(streetNameList.get((int) (Math.random() * (streetNameList.size() - 1))))
                .setStreetNumber(genericNumberList.get((int) (Math.random() * (genericNumberList.size() - 1))))
                .setEstablishmentType(establishmentTypeList.get((int) (Math.random() * (establishmentTypeList.size() - 1))))
                .setEstablishmentName(establishmentNameList.get((int) (Math.random() * (establishmentNameList.size() - 1))))
                .setEstablishmentNumber(genericNumberList.get((int) (Math.random() * (genericNumberList.size() - 1))))
                .setAddressDetail(addressDetailList.get((int) (Math.random() * (addressDetailList.size() - 1))))
                .setZipCode(zipCodeList.get((int) (Math.random() * (zipCodeList.size() - 1))))
                .isDaultLocation(false)
                .setLocale("en_US");
        return builder;
    }
}
