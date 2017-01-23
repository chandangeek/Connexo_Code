/*
 * ValueInformationfieldCoding.java
 *
 * Created on 3 oktober 2007, 17:32
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author kvds
 */
public class ValueInformationfieldCoding {

    static public final int TYPE_UNIT=0;
    static public final int TYPE_A=1;
    static public final int TYPE_B=2;
    static public final int TYPE_C=3;
    static public final int TYPE_D=4;
    static public final int TYPE_E=5;
    static public final int TYPE_F=6;
    static public final int TYPE_G=7;
    static public final int TYPE_H=8;
    static public final int TYPE_I=9;
    static public final int TYPE_J=10;
    static public final int TYPE_K=11;
    static public final int TYPE_L=12;
    static public final int TYPE_DURATION=13;

    static List primaryVIFs = new ArrayList();
    static List fdVIFEs = new ArrayList();
    static List fbVIFEs = new ArrayList();
    static List manufacturerVIFEs = new ArrayList();
    static List combinableVIFEs = new ArrayList();
    static {


        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x00,0x78,-3,Unit.get(BaseUnit.WATTHOUR),TYPE_UNIT,"Energy",new ObisCodeCreator().setB(1).setC(1).setD(8).setE(0).setF(255)));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x08,0x78,0,Unit.get(BaseUnit.JOULE),TYPE_UNIT,"Energy",new ObisCodeCreator().setB(1).setC(1).setD(8).setE(0).setF(255)));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x10,0x78,-6,Unit.get(BaseUnit.CUBICMETER),TYPE_UNIT,"Volume",new ObisCodeCreator().setB(1).setC(1).setD(8).setE(0).setF(255)));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x18,0x78,-3,Unit.get(BaseUnit.KILOGRAM),TYPE_UNIT,"Mass",new ObisCodeCreator().setB(1).setC(1).setD(8).setE(0).setF(255)));
        primaryVIFs.add(addDurationValueInformationfieldCoding(0x20,0x7C,Unit.get(BaseUnit.SECOND),TYPE_DURATION,"Duration of meter power up,null",null));
        primaryVIFs.add(addDurationValueInformationfieldCoding(0x24,0x7C,Unit.get(BaseUnit.SECOND),TYPE_DURATION,"Duration of meter accumulation",null));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x28,0x78,-3,Unit.get(BaseUnit.WATT),TYPE_UNIT,"Power",new ObisCodeCreator(-1, 0, 1, 7, 0, 255)));      //Power: D field = 7
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x30,0x78,0,Unit.get(BaseUnit.JOULEPERHOUR),TYPE_UNIT,"Power",null));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x38,0x78,-6,Unit.get(BaseUnit.CUBICMETERPERHOUR),TYPE_UNIT,"Volume Flow", new ObisCodeCreator().setB(0).setC(4).setD(0).setE(0).setF(255)));
        primaryVIFs.add(addPower10Mul60ValueInformationfieldCoding(0x40,0x78,-7,Unit.get(BaseUnit.CUBICMETERPERHOUR),TYPE_UNIT,"Volume Flow ext.",null));
        primaryVIFs.add(addPower10Mul3600ValueInformationfieldCoding(0x48,0x78,-9,Unit.get(BaseUnit.CUBICMETERPERHOUR),TYPE_UNIT,"Volume Flow ext.",null));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x50,0x78,-3,Unit.get(BaseUnit.KILOGRAMPERHOUR),TYPE_UNIT,"Mass Flow",null));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x58,0x7C,-3,Unit.get(BaseUnit.DEGREE_CELSIUS),TYPE_UNIT,"Flow Temperature", new ObisCodeCreator().setB(0).setC(10).setD(0).setE(0).setF(255)));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x5C,0x7C,-3,Unit.get(BaseUnit.DEGREE_CELSIUS),TYPE_UNIT,"Return Temperature",new ObisCodeCreator().setB(0).setC(11).setD(0).setE(0).setF(255)));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x60,0x7C,-3,Unit.get(BaseUnit.KELVIN),TYPE_UNIT,"Temperature Difference",new ObisCodeCreator().setB(0).setC(12).setD(0).setE(0).setF(255)));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x64,0x7C,-3,Unit.get(BaseUnit.DEGREE_CELSIUS),TYPE_UNIT,"External Temperature",null));
        primaryVIFs.add(addPower10ValueInformationfieldCoding(0x68,0x7C,-3,Unit.get(BaseUnit.BAR),TYPE_UNIT,"Pressure",null));
        primaryVIFs.add(addValueInformationfieldCoding(0x6C,0x7F,0x02,Unit.get(BaseUnit.UNITLESS),TYPE_G,"Date (actual or associated with a storage number/function",null));

        primaryVIFs.add(addValueInformationfieldCoding(0x6D,0x7F,0x04,Unit.get(BaseUnit.UNITLESS),TYPE_F,"Date and Time (actual or associated with a storage number/function",null));
        primaryVIFs.add(addValueInformationfieldCoding(0x6D,0x7F,0x05,Unit.get(BaseUnit.UNITLESS),TYPE_F,"Date and Time (actual or associated with a storage number/function",null));
        primaryVIFs.add(addValueInformationfieldCoding(0x6D,0x7F,0x03,Unit.get(BaseUnit.UNITLESS),TYPE_F,"Extended Time point (actual or associated with a storage number/function",null));
        primaryVIFs.add(addValueInformationfieldCoding(0x6D,0x7F,0x06,Unit.get(BaseUnit.UNITLESS),TYPE_F,"Extended Date and Time point (actual or associated with a storage number/function",null));

        primaryVIFs.add(addValueInformationfieldCoding(0x6E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Units for H.C.A.",null));
        primaryVIFs.add(addValueInformationfieldCoding(0x6F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Reserved for a future third table of VIF-extensions",null));
        primaryVIFs.add(addDurationValueInformationfieldCoding(0x70,0x7C,Unit.get(BaseUnit.SECOND),TYPE_DURATION,"Averaging Duration",null));
        primaryVIFs.add(addDurationValueInformationfieldCoding(0x74,0x7C,Unit.get(BaseUnit.SECOND),TYPE_DURATION,"Actuality Duration",null));
        primaryVIFs.add(addValueInformationfieldCoding(0x78,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Fabrication Number", new ObisCodeCreator(0, 1, 24, 1, 0, 255)));
        primaryVIFs.add(addValueInformationfieldCoding(0x79,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"(Enhanced) Identification",null));
        primaryVIFs.add(addValueInformationfieldCoding(0x7A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Address",null));
        primaryVIFs.add(addValueInformationfieldCoding(0x7B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Extension to VIF codes FB",null));
        primaryVIFs.add(addValueInformationfieldCoding(0x7D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Extension to VIF codes FD",null));

        fdVIFEs.add(addPower10ValueInformationfieldCoding(0x00,0x7C,-3,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Local Currency Credit",null));
        fdVIFEs.add(addPower10ValueInformationfieldCoding(0x04,0x7C,-3,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Local Currency Debit",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x08,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Access Number (transmission count)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x09,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Device type",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x0A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Manufacturer",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x0B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Parameter set identification",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x0C,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Model/Version",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x0D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Hardware Version",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x0E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Metrology Firmware Version", new ObisCodeCreator(-1, 0, 0, 2, 1, 255)));
        fdVIFEs.add(addValueInformationfieldCoding(0x0F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Other software version", new ObisCodeCreator(-1, 0, 0, 2, 2, 255)));
        fdVIFEs.add(addValueInformationfieldCoding(0x10,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Customer Location",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x11,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Customer",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x12,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Access Code User",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x13,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Access Code Operator",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x14,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Access Code system Operator",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x15,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Access Code developer",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x16,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Password",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x17,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Error flags (binary)(device type specific)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x18,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Error mask",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x1A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Digital output (binary)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x1B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Digital input (binary)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x1C,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Baud rate (baud)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x1D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Response delay time (bittimes)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x1E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Retry",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x1F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Remote control (device specific)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x20,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"First storage # for cyclic storage",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x21,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Last storage # for cyclic storage",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x22,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Size of storage block",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x24,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Storage interval sec(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x25,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Storage interval minute(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x26,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Storage interval hour(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x27,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Storage interval day(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x28,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Storage interval [month(s)]",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x29,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Storage intervals [year(s)]",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x2B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Time point second (0...59)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x2C,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration since last readout sec(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x2D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration since last readout minute(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x2E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration since last readout hour(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x2F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration since last readout day(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x30,0x7F,2,Unit.get(BaseUnit.UNITLESS),TYPE_G,"Start (date/time) of tariff",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x30,0x7F,3,Unit.get(BaseUnit.UNITLESS),TYPE_J,"Start (date/time) of tariff",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x30,0x7F,4,Unit.get(BaseUnit.UNITLESS),TYPE_F,"Start (date/time) of tariff",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x31,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of tariff (minutes)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x32,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of tariff (hours)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x33,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of tariff (days)",null));

        fdVIFEs.add(addValueInformationfieldCoding(0x34,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Period of tariff (seconds)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x35,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Period of tariff (minutes)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x36,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Period of tariff (hours)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x37,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Period of tariff (days)",null));

        fdVIFEs.add(addValueInformationfieldCoding(0x38,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Period of tariff [month(s)]",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x39,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Period of tariff [year(s)]",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x3A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Dimensionless / no VIF",null));
        fdVIFEs.add(addPower10ValueInformationfieldCoding(0x40,0x70,-9,Unit.get(BaseUnit.VOLT),TYPE_UNIT,"Volts",null));
        fdVIFEs.add(addPower10ValueInformationfieldCoding(0x50,0x70,-12,Unit.get(BaseUnit.AMPERE),TYPE_UNIT,"Amperes",null));

        fdVIFEs.add(addValueInformationfieldCoding(0x60,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Reset counter",null));

        fdVIFEs.add(addValueInformationfieldCoding(0x61,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Cumulation counter",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x62,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Control signal",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x63,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_A,"Day of week",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x64,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Week number",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x65,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Time point of day change",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x66,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"State of parameter activation",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x67,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Special supplier information",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x68,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration since last cumulation hour(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x69,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration since last cumulation day(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x6A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration since last cumulation month(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x6B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration since last cumulation year(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x6C,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Operating time battery hour(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x6D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Operating time battery day(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x6E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Operating time battery month(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x6F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Operating time battery year(s)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x70,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Date and time of battery change",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x71,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Reserved",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x72,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_K,"Day light saving (beginning,ending,deviation) data type K",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x73,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_L,"Listening window management type L",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x74,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Remaining battery life time (days)",null));
        fdVIFEs.add(addValueInformationfieldCoding(0x75,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"# times the meter was stopped",null));


        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x00,0x7E,-1,Unit.get(BaseUnit.WATTHOUR, 6),TYPE_UNIT,"Energy",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x01,0x7E,0,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3),TYPE_UNIT,"Reactive Energy",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x08,0x7E,-1,Unit.get(BaseUnit.JOULE,9),TYPE_UNIT,"Energy",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x10,0x7E,2,Unit.get(BaseUnit.CUBICMETER),TYPE_UNIT,"Volume",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x18,0x7E,2,Unit.get(BaseUnit.TON),TYPE_UNIT,"Mass",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x21,0x7F,0,Unit.get(BaseUnit.CUBICFEET,-1),TYPE_UNIT,"Volume",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x22,0x7F,0,Unit.get(BaseUnit.GALLON,-1),TYPE_UNIT,"Volume",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x23,0x7F,0,Unit.get(BaseUnit.GALLON),TYPE_UNIT,"Volume",null));
        fbVIFEs.add(addPower10Mul60ValueInformationfieldCoding(0x24,0x7F,0,Unit.get(BaseUnit.GALLONPERHOUR, -3),TYPE_UNIT,"Volume flow",null));
        fbVIFEs.add(addPower10Mul60ValueInformationfieldCoding(0x25,0x7F,0,Unit.get(BaseUnit.GALLONPERHOUR),TYPE_UNIT,"Volume flow",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x26,0x7F,0,Unit.get(BaseUnit.GALLONPERHOUR),TYPE_UNIT,"Volume flow",null));

        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x28,0x7E,-1,Unit.get(BaseUnit.WATT, 6),TYPE_UNIT,"Power",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x30,0x7E,-1,Unit.get(BaseUnit.JOULEPERHOUR, 9),TYPE_UNIT,"Power",null));

        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x58,0x7C,-3,Unit.get(BaseUnit.FAHRENHEIT),TYPE_UNIT,"Flow Temperature",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x5C,0x7C,-3,Unit.get(BaseUnit.FAHRENHEIT),TYPE_UNIT,"Flow Temperature",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x60,0x7C,-3,Unit.get(BaseUnit.FAHRENHEIT),TYPE_UNIT,"Flow Temperature",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x64,0x7C,-3,Unit.get(BaseUnit.FAHRENHEIT),TYPE_UNIT,"Flow Temperature",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x70,0x7C,-3,Unit.get(BaseUnit.FAHRENHEIT),TYPE_UNIT,"Cold/Warm Temperature Limit",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x74,0x7C,-3,Unit.get(BaseUnit.DEGREE_CELSIUS),TYPE_UNIT,"Cold/Warm Temperature Limit",null));
        fbVIFEs.add(addPower10ValueInformationfieldCoding(0x78,0x78,-3,Unit.get(BaseUnit.WATT),TYPE_UNIT,"Cum Count max power",null));

        // record error codes
        combinableVIFEs.add(addValueInformationfieldCoding(0x00,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"None",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x01,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Too many DIFE's",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x02,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Storage number not implemented",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x03,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Unit number not implemented",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x04,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Tariff number not implemented",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x05,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Function not implemented",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x06,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Data class not implemented",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x07,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Data size not implemented",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x0b,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Too many VIFE's",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x0c,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Illegal VIF-Group",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x0d,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Illegal VIF-Exponent",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x0e,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"VIF/DIF mismatch",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x0f,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Unimplemented action",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x15,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"No data available (undefined value)",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x16,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Data overflow",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x17,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Data underflow",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x18,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Data error",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x1C,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Premature end of record",null));


        combinableVIFEs.add(addValueInformationfieldCoding(0x20,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per second",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x21,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per minute",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x22,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per hour",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x23,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per day",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x24,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per week",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x25,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per month",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x26,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per year",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x27,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per revolution / measurement",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x28,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"increment per input pulse on input channel 0",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x29,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"increment per input pulse on input channel 1",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x2A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"increment per output pulse on output channel 0",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x2B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"increment per output pulse on output channel 1",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x2C,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per liter",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x2D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per m3",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x2E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per kg",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x2F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per K (Kelvin)",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x30,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per kWh",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x31,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per GJ",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x32,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per kW",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x33,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per (K*l) (Kelvin*liter)",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x34,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per V (Volt)",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x35,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"per A (Ampere)",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x36,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"multiplied by s",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x37,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"multiplied by s / V",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x38,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"multiplied by s / A",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x39,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"start date(/time) of a, b",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x3A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"VIF contains uncorrected unit instead of corrected unit",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x3B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"accumulation only if positive contributions (Forward flow contribution)",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x3C,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"accumulation of abs value only if negative contributions (Backward flow)",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x3D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"reserved for alternate non-metric unit system (see annex C)",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x40,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"lower limit value",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x48,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"upper limit value",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x41,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"# of exceeds lower limit",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x49,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"# of exceeds upper limit",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x42,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Begin date/time of first lower limit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x43,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"End date/time of first lower limit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x46,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Begin date/time of last lower limit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x47,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"End date/time of last lower limit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x4A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Begin date/time of first upper linit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x4B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"End date/time of first upper linit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x4E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Begin date/time of last upper linit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x4F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"End date/time of last upper linit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x50,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of first lower limit 0",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x51,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of first lower limit 1",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x52,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of first lower limit 2",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x53,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of first lower limit 3",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x54,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of last lower limit 0",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x55,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of last lower limit 1",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x56,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of last lower limit 2",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x57,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of last lower limit 3",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x58,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of first upper limit 0",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x59,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of first upper limit 1",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x5A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of first upper limit 2",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x5B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of first upper limit 3",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x5C,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of last upper limit 0",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x5D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of last upper limit 1",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x5E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of last upper limit 2",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x5F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Duration of last upper limit 3",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x60,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"First duration of a, b 0",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x61,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"First duration of a, b 1",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x62,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"First duration of a, b 2",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x63,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"First duration of a, b 3",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x64,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Last duration of a, b 0",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x65,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Last duration of a, b 1",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x66,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Last duration of a, b 2",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x67,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Last duration of a, b 3",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x68,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Value during lower limit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x6C,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Value during upper limit exceed",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x69,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Leakage values",null));

        combinableVIFEs.add(addValueInformationfieldCoding(0x6D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Overflow values",null));

        combinableVIFEs.add(addValueInformationfieldCoding(0x6A,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Begin date (/time) of first",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x6B,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"End date (/time) of first",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x6E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Begin date (/time) of last",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x6F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"End date (/time) of last",null));
        combinableVIFEs.add(addPower10ValueInformationfieldCoding(0x70,0x78,-6,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Multiplicative correction factor",null));
        combinableVIFEs.add(addPower10ValueInformationfieldCoding(0x78,0x7C,-3,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Additive correction constant. unit of VIF (offset)",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x7D,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Multiplicative correction factor for value (not unit): 103",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x7E,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Future value",null));
        combinableVIFEs.add(addValueInformationfieldCoding(0x7F,0x7F,Unit.get(BaseUnit.UNITLESS),TYPE_UNIT,"Next VIFE's and data of this block are maufacturer specific",null));

    }

    private int id;
    private int mask;
    private Unit unit;
    private int type;
    private String description;
    private RangeCoder rangeCoder;
    int coding=-1;
    private int difDataField=-1;
    private ObisCodeCreator obisCodeCreator;

    /** Creates a new instance of ValueInformationfieldCoding */
    private ValueInformationfieldCoding(int id, int mask, Unit unit,  int type, String description, ObisCodeCreator obisCodeCreator) {
        this.setId(id);
        this.setMask(mask);
        this.setUnit(unit);
        this.setType(type);
        this.setDescription(description);
        this.setObisCodeCreator(obisCodeCreator);
    }
    private ValueInformationfieldCoding(int id, int mask, int difDataField, Unit unit,  int type, String description, ObisCodeCreator obisCodeCreator) {
        this.setId(id);
        this.setMask(mask);
        this.setUnit(unit);
        this.setType(type);
        this.setDifDataField(difDataField);
        this.setDescription(description);
        this.setObisCodeCreator(obisCodeCreator);
    }

    public boolean isTypeUnit() {
        return getType() == TYPE_UNIT;
    }

    public boolean isTypeDuration() {
        return getType() == TYPE_DURATION;
    }

    public boolean isTypeFormat() {
        return (getType() != TYPE_UNIT) && (getType() != TYPE_DURATION);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ValueInformationfieldCoding:\n");
        strBuff.append("   description="+getDescription()+"\n");
        strBuff.append("   difDataField="+getDifDataField()+"\n");
        strBuff.append("   id="+getId()+"\n");
        strBuff.append("   mask="+getMask()+"\n");
        strBuff.append("   multiplier="+getMultiplier()+"\n");
        //strBuff.append("   rangeCoder="+getRangeCoder()+"\n");
        strBuff.append("   type="+getType()+"\n");
        strBuff.append("   unit="+getUnit()+"\n");
        return strBuff.toString();
    }

    public boolean isCodingExtended() {
        return (coding & 0x80) == 0x80;
    }

    static private ValueInformationfieldCoding addValueInformationfieldCoding(int id, int mask, Unit unit,  int type, String description, ObisCodeCreator obisCodeCreator) {
        ValueInformationfieldCoding v = new ValueInformationfieldCoding(id,mask,unit,type,description,obisCodeCreator);
        return v;
    }
    static private ValueInformationfieldCoding addValueInformationfieldCoding(int id, int mask, int difDatafield, Unit unit,  int type, String description, ObisCodeCreator obisCodeCreator) {
        ValueInformationfieldCoding v = new ValueInformationfieldCoding(id,mask,difDatafield,unit,type,description,obisCodeCreator);
        return v;
    }

    static private ValueInformationfieldCoding addPower10Mul3600ValueInformationfieldCoding(int id, int mask, int multiplier, Unit unit,  int type, String description, ObisCodeCreator obisCodeCreator) {
        ValueInformationfieldCoding v = new ValueInformationfieldCoding(id,mask,unit,type,description,obisCodeCreator);
        v.setRangeCoder(v.createPower10Mul3600RangeCoder(multiplier));
        return v;
    }

    static private ValueInformationfieldCoding addPower10Mul60ValueInformationfieldCoding(int id, int mask, int multiplier, Unit unit,  int type, String description, ObisCodeCreator obisCodeCreator) {
        ValueInformationfieldCoding v = new ValueInformationfieldCoding(id,mask,unit,type,description,obisCodeCreator);
        v.setRangeCoder(v.createPower10Mul60RangeCoder(multiplier));
        return v;
    }

    static private ValueInformationfieldCoding addPower10ValueInformationfieldCoding(int id, int mask, int multiplier, Unit unit,  int type, String description, ObisCodeCreator obisCodeCreator) {
        ValueInformationfieldCoding v = new ValueInformationfieldCoding(id,mask,unit,type,description,obisCodeCreator);
        v.setRangeCoder(v.createPower10RangeCoder(multiplier));
        return v;
    }

    static private ValueInformationfieldCoding addDurationValueInformationfieldCoding(int id, int mask, Unit unit,  int type, String description, ObisCodeCreator obisCodeCreator) {
        ValueInformationfieldCoding v = new ValueInformationfieldCoding(id,mask,unit,type,description,obisCodeCreator);
        v.setRangeCoder(v.createDurationRangeCoder());
        return v;
    }

    private RangeCoder createPower10RangeCoder(final int multiplier) {
        return new RangeCoder() {
            public BigDecimal calcMultiplier(int coding) {
                double d = Math.pow(10, (coding&((getMask()|0x80)^0xff))+multiplier);
                BigDecimal bd = new BigDecimal(""+d);
                return bd;
            }
        };
    }

    private RangeCoder createPower10Mul60RangeCoder(final int multiplier) {
        return new RangeCoder() {
            public BigDecimal calcMultiplier(int coding) {
                double d = Math.pow(10, (coding&((getMask()|0x80)^0xff))+multiplier);
                BigDecimal bd = new BigDecimal(""+d);
                bd = bd.multiply(new BigDecimal(60));
                return bd;
            }
        };
    }

    private RangeCoder createPower10Mul3600RangeCoder(final int multiplier) {
        return new RangeCoder() {
            public BigDecimal calcMultiplier(int coding) {
                double d = Math.pow(10, (coding&((getMask()|0x80)^0xff))+multiplier);
                BigDecimal bd = new BigDecimal(""+d);
                bd = bd.multiply(new BigDecimal(3600));
                return bd;
            }
        };
    }

    private RangeCoder createDurationRangeCoder() {
        return new RangeCoder() {
            public BigDecimal calcMultiplier(int coding) {
                int range = (coding&((getMask()|0x80)^0xff));
                BigDecimal bd = BigDecimal.ONE;
                if (range==1) {
                    //bd = new BigDecimal(60);
                    setUnit(Unit.get(BaseUnit.MINUTE));
                }
                else if (range==2) {
                    //bd = new BigDecimal(60*60);
                    setUnit(Unit.get(BaseUnit.HOUR));
                }
                else if (range==3) {
                    //bd = new BigDecimal(60*60*24);
                    setUnit(Unit.get(BaseUnit.DAY));
                }
                return bd;
            }
        };
    }

    public BigDecimal getMultiplier() {
        if (rangeCoder != null)
            return rangeCoder.calcMultiplier(coding);
        else
            return BigDecimal.ONE;
    }

    static public ValueInformationfieldCoding findPrimaryValueInformationfieldCoding(int coding, int dataField) throws IOException {
        Iterator it = primaryVIFs.iterator();
        while(it.hasNext()) {
            ValueInformationfieldCoding v =(ValueInformationfieldCoding)it.next();
            if (((v.getId()) == (coding&v.getMask())) &&  (v.getDifDataField()==-1)) {
                v.coding=coding;
                return v;
            }
            else if (((v.getId()) == (coding&v.getMask())) &&  (v.getDifDataField()==dataField)) {
                v.coding=coding;
                return v;
            }
        }
        throw new IOException("ValueInformationfieldCoding, findPrimaryValueInformationfieldCoding, invalid coding + "+coding);
    }

    static public ValueInformationfieldCoding findFDExtensionValueInformationfieldCoding(int coding, int dataField) throws IOException {
        Iterator it = fdVIFEs.iterator();
        while(it.hasNext()) {
            ValueInformationfieldCoding v =(ValueInformationfieldCoding)it.next();
            if (((v.getId()) == (coding&v.getMask())) &&  (v.getDifDataField()==-1)) {
                v.coding=coding;
                return v;
            }
            else if (((v.getId()) == (coding&v.getMask())) &&  (v.getDifDataField()==dataField)) {
                v.coding=coding;
                return v;
            }
        }
        throw new IOException("ValueInformationfieldCoding, findFDExtensionValueInformationfieldCoding, invalid coding + "+coding);
    }

    static public ValueInformationfieldCoding findFBExtensionValueInformationfieldCoding(int coding, int dataField) throws IOException {
        Iterator it = fbVIFEs.iterator();
        while(it.hasNext()) {
            ValueInformationfieldCoding v =(ValueInformationfieldCoding)it.next();
            if (((v.getId()) == (coding&v.getMask())) &&  (v.getDifDataField()==-1)) {
                v.coding=coding;
                return v;
            }
            else if (((v.getId()) == (coding&v.getMask())) &&  (v.getDifDataField()==dataField)) {
                v.coding=coding;
                return v;
            }
        }
        throw new IOException("ValueInformationfieldCoding, findFBExtensionValueInformationfieldCoding, invalid coding + "+coding);
    }

    static public ValueInformationfieldCoding findCombinableExtensionValueInformationfieldCoding(int coding, int dataField) throws IOException {
        Iterator it = combinableVIFEs.iterator();
        while(it.hasNext()) {
            ValueInformationfieldCoding v =(ValueInformationfieldCoding)it.next();
            if (((v.getId()) == (coding&v.getMask())) &&  (v.getDifDataField()==-1)) {
                v.coding=coding;
                return v;
            }
            else if (((v.getId()) == (coding&v.getMask())) &&  (v.getDifDataField()==dataField)) {
                v.coding=coding;
                return v;
            }
        }
        throw new IOException("ValueInformationfieldCoding, findCombinableExtensionValueInformationfieldCoding, invalid coding + "+coding);
    }


    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public RangeCoder getRangeCoder() {
        return rangeCoder;
    }

    public void setRangeCoder(RangeCoder rangeCoder) {
        this.rangeCoder = rangeCoder;
    }

    public int getDifDataField() {
        return difDataField;
    }

    public void setDifDataField(int difDataField) {
        this.difDataField = difDataField;
    }

    public ObisCodeCreator getObisCodeCreator() {
        return obisCodeCreator;
    }

    public void setObisCodeCreator(ObisCodeCreator obisCodeCreator) {
        this.obisCodeCreator = obisCodeCreator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
