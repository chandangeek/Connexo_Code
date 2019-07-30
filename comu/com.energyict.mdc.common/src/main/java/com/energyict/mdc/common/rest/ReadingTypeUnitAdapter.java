/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.cbo.ReadingTypeUnit;

public class ReadingTypeUnitAdapter extends MapBasedXmlAdapter<ReadingTypeUnit> {

    public ReadingTypeUnitAdapter() {
        register("", ReadingTypeUnit.NOTAPPLICABLE);
        register("not applicable", ReadingTypeUnit.NOTAPPLICABLE);
        register("meter (m)",ReadingTypeUnit.METER);
        register("gram (g)",ReadingTypeUnit.GRAM);
        register("rotations per second (rev/s)",ReadingTypeUnit.ROTATIONSPERSECOND);
        register("ampere (A)",ReadingTypeUnit.AMPERE);
        register("kelvin (K)",ReadingTypeUnit.KELVIN);
        register("mole (mol)",ReadingTypeUnit.MOLE);
        register("candela (cd)",ReadingTypeUnit.CANDELA);
        register("degrees (\u00b0)",ReadingTypeUnit.DEGREES);
        register("radian (rad)",ReadingTypeUnit.RADIAN);
        register("steradian (sr)",ReadingTypeUnit.STERADIAN);
        register("gray (Gy)",ReadingTypeUnit.GRAY);
        register("becquerel (Bq)",ReadingTypeUnit.BECQUEREL);
        register("degrees Celsius (\u00b0C)",ReadingTypeUnit.DEGREESCELSIUS);
        register("sievert (Sv)",ReadingTypeUnit.SIEVERT);
        register("farad (F)",ReadingTypeUnit.FARAD);
        register("coulomb (C)",ReadingTypeUnit.COULOMB);
        register("second (s)",ReadingTypeUnit.SECOND);
        register("henry (H)",ReadingTypeUnit.HENRY);
        register("volt (V)",ReadingTypeUnit.VOLT);
        register("ohm (\u03a9)",ReadingTypeUnit.OHM);
        register("joule (J)",ReadingTypeUnit.JOULE);
        register("newton (N)",ReadingTypeUnit.NEWTON);
        register("hertz (Hz)",ReadingTypeUnit.HERTZ);
        register("lux (lx)",ReadingTypeUnit.LUX);
        register("lumen (Lm)",ReadingTypeUnit.LUMEN);
        register("weber (Wb)",ReadingTypeUnit.WEBER);
        register("tesla (T)",ReadingTypeUnit.TESLA);
        register("watt (W)",ReadingTypeUnit.WATT);
        register("pascal (Pa)",ReadingTypeUnit.PASCAL);
        register("square meter (m\u00b2)",ReadingTypeUnit.SQUAREMETER);
        register("cubic meter (m\u00b3)",ReadingTypeUnit.CUBICMETER);
        register("meter per second (m/s)",ReadingTypeUnit.METERPERSECOND);
        register("meter per second squared (m/s\u00b2)",ReadingTypeUnit.METERPERSECONDSQUARED);
        register("cubic meter per second (m\u00b3/s)",ReadingTypeUnit.CUBICMETERPERSECOND);
        register("meters per cubic meter (m/m\u00b3)",ReadingTypeUnit.METERPERCUBICMETER);
        register("gram meter (gm)",ReadingTypeUnit.GRAMMETER);
        register("gram per cubic meter (g/m\u00b3)",ReadingTypeUnit.GRAMPERCUBICMETER);
        register("meter squared per second (m\u00b2/s)",ReadingTypeUnit.METERSQUAREDPERSECOND);
        register("watt per meter kelvin (W/(mK))",ReadingTypeUnit.WATTPERMETERKELVIN);
        register("joule per kelvin (J/K)",ReadingTypeUnit.JOULEPERKELVIN);
        register("siemens (S)",ReadingTypeUnit.SIEMENS);
        register("radians per second (rad/s)",ReadingTypeUnit.RADIANSPERSECOND);
        register("volt ampere (VA)",ReadingTypeUnit.VOLTAMPERE);
        register("volt ampere reactive (VAr)",ReadingTypeUnit.VOLTAMPEREREACTIVE);
        register("phase angle (\u03b8-Deg)",ReadingTypeUnit.PHASEANGLE);
        register("power factor (Cos \u03b8)",ReadingTypeUnit.POWERFACTOR);
        register("volt seconds (Vs)",ReadingTypeUnit.VOLTSECONDS);
        register("volt squared (V\u00b2)",ReadingTypeUnit.VOLTSQUARED);
        register("ampere seconds (As)",ReadingTypeUnit.AMPERESECONDS);
        register("ampere squared (A\u00b2)",ReadingTypeUnit.AMPERESQUARED);
        register("ampere squared second (A\u00b2s)",ReadingTypeUnit.AMPERESQUAREDSECOND);
        register("volt ampere hours (VAh)",ReadingTypeUnit.VOLTAMPEREHOUR);
        register("watt hours (Wh)",ReadingTypeUnit.WATTHOUR);
        register("volt ampere reactive hours (VArh)",ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR);
        register("volts per hertz (V/Hz)",ReadingTypeUnit.VOLTPERHERZ);
        register("hertz per second (Hz/s)",ReadingTypeUnit.HERZPERSECOND);
        register("characters (char)",ReadingTypeUnit.CHARACTERS);
        register("characters per second (char/s)",ReadingTypeUnit.CHARACTERSPERSECOND);
        register("gram meter squared (gm\u00b2)",ReadingTypeUnit.GRAMMETERSQUARED);
        register("bel (B)",ReadingTypeUnit.BEL);
        register("money (\u00a4)",ReadingTypeUnit.MONEY);
        register("watt per second (W/s)",ReadingTypeUnit.WATTPERSECOND);
        register("littre per second (L/s)",ReadingTypeUnit.LITREPERSECOND);
        register("quantity power (Q)",ReadingTypeUnit.QUANTITYPOWER);
        register("quantity energy (Qh)",ReadingTypeUnit.QUANTITYENERGY);
        register("ohm meter (\u03a9m)",ReadingTypeUnit.OHMMETER);
        register("ampere per meter (A/m)",ReadingTypeUnit.AMPEREPERMETER);
        register("volt squared hour (V\u00b2h)",ReadingTypeUnit.VOLTSQUAREDHOUR);
        register("ampere squared hour (A\u00b2h)",ReadingTypeUnit.AMPERESQUAREDHOUR);
        register("ampere hour (Ah)",ReadingTypeUnit.AMPEREHOUR);
        register("watt hour per cubic meter (Wh/m\u00b3)",ReadingTypeUnit.WATTHOURPERCUBICMETER);
        register("timestamp (timeStamp)",ReadingTypeUnit.TIMESTAMP);
        register("boolean (status)",ReadingTypeUnit.BOOLEAN);
        register("boolean array (statuses)",ReadingTypeUnit.BOOLEANARRAY);
        register("count (Count)",ReadingTypeUnit.COUNT);
        register("bel milliwatt (Bm)",ReadingTypeUnit.BELMILLIWATT);
        register("bel microvolt (BmV)",ReadingTypeUnit.BELMICROVOLT);
        register("encoded value (Code)",ReadingTypeUnit.ENCODEDVALUE);
        register("watt hours per rotation (Wh/rev)",ReadingTypeUnit.WATTHOURPERROTATION);
        register("volt ampere reactive hours per rotation (VArh/rev)",ReadingTypeUnit.VOLTAMPEREREACTIVEHOURPERROTATION);
        register("volt ampere hours per rotation (VAh/rev)",ReadingTypeUnit.VOLTAMPEREHOURPERROTATION);
        register("end device event code (MeCode)",ReadingTypeUnit.ENDDEVICEEVENTCODE);
        register("cubic feet (ft\u00b3)",ReadingTypeUnit.CUBICFEET);
        register("cubic feet compensated (ft\u00b3(compensated))",ReadingTypeUnit.CUBICFEETCOMPENSATED);
        register("cubic feet uncompensated (ft\u00b3(uncompensated))",ReadingTypeUnit.CUBICFEETUNCOMPENSATED);
        register("cubic feet per hour (ft\u00b3/h)",ReadingTypeUnit.CUBICFEETPERHOUR);
        register("cubic feet compensated per hour (ft\u00b3(compensated)/h)",ReadingTypeUnit.CUBICFEETCOMPENSATEDPERHOUR);
        register("cubic feet uncompensated per hour (ft\u00b3(uncompensated)/h)",ReadingTypeUnit.CUBICFEETUNCOMPENSATEDPERHHOUR);
        register("cubic meter per hour (m\u00b3/h)",ReadingTypeUnit.CUBICMETERPERHOUR);
        register("cubic meter compensated per hour (m\u00b3(compensated)/h)",ReadingTypeUnit.CUBICMETERPERHOURCOMPENSATED);
        register("cubic meter uncompensated per hour (m\u00b3(uncompensated)/h)",ReadingTypeUnit.CUBICMETERPERHOURUNCOMPENSATED);
        register("us gallon (USGal)",ReadingTypeUnit.USGALLON);
        register("us gallon per hour (USGal/h)",ReadingTypeUnit.USGALLONPERHOUR);
        register("imperial gallon (ImperialGal)",ReadingTypeUnit.IMPERIALGALLON);
        register("imperial gallon per hour (ImperialGal/h)",ReadingTypeUnit.IMPERIALGALLONPERHOUR);
        register("british thermal unit (BTU)",ReadingTypeUnit.BRITISHTHERMALUNIT);
        register("british thermal unit per hour (BTU/h)",ReadingTypeUnit.BRITISHTHERMALUNITPERHOUR);
        register("litre (L)",ReadingTypeUnit.LITRE);
        register("litre per hour (L/h)",ReadingTypeUnit.LITREPERHOUR);
        register("litre compensated per hour (L(compensated)/h)",ReadingTypeUnit.LITREPERHOURCOMPENSATED);
        register("litre uncompensated per hour (L(uncompensated)/h)",ReadingTypeUnit.LITREPERHOURUNCOMPENSATED);
        register("pascal gauge (PaG)",ReadingTypeUnit.PASCALGUAGE);
        register("pounds per square inch, absolute (ps/A)",ReadingTypeUnit.POUNDPERSQUAREINCHABSOLUTE);
        register("pounds per square inch, gauge (ps/G)",ReadingTypeUnit.POUNDPERSQUAREINCHGAUGE);
        register("litre per litre",ReadingTypeUnit.LITREPERLITRE);
        register("g per g",ReadingTypeUnit.GPERG);
        register("mol per m3 (mol/m\u00b3)",ReadingTypeUnit.MOLPERM3);
        register("mol per mol",ReadingTypeUnit.MOLPERMOL);
        register("mol per kg (mol/kg)",ReadingTypeUnit.MOLPERKG);
        register("meter per meter (m/m)",ReadingTypeUnit.METERPERMETER);
        register("second per second (s/s)",ReadingTypeUnit.SECONDPERSECOND);
        register("herz per herz (Hz/Hz)",ReadingTypeUnit.HERZPERHERZ);
        register("volt per volt (V/V)",ReadingTypeUnit.VOLTPERVOLT);
        register("ampere per ampere (A/A)",ReadingTypeUnit.AMPEREPERAMPERE);
        register("watt per voltampere (W/VA)",ReadingTypeUnit.WATTPERVOLTAMPERE);
        register("revolutions (rev)",ReadingTypeUnit.REVOLUTIONS);
        register("pascal absolute (PaA)",ReadingTypeUnit.PASCALABSOLUTE);
        register("litre uncompensated (L(uncompensated))",ReadingTypeUnit.LITREUNCOMPENSATED);
        register("litre compensated (L(compensated))",ReadingTypeUnit.LITRECOMPENSATED);
        register("katal (kat)",ReadingTypeUnit.KATAL);
        register("minute (min)",ReadingTypeUnit.MINUTE);
        register("hour (h)",ReadingTypeUnit.HOUR);
        register("quantity power 45 (Q45)",ReadingTypeUnit.QUANTITYPOWER45);
        register("quantity power 60 (Q60)",ReadingTypeUnit.QUANTITYPOWER60);
        register("quantity energy 45 (Q45h)",ReadingTypeUnit.QUANTITYENERGY45);
        register("quantity energy 60 (Q60h)",ReadingTypeUnit.QUANTITYENERGY60);
        register("joule per kg (J/kg)",ReadingTypeUnit.JOULEPERKG);
        register("cubic meter uncompensated (m\u00b3(uncompensated))",ReadingTypeUnit.CUBICMETERUNCOMPENSATED);
        register("cubic meter compensated (m\u00b3(compensated))",ReadingTypeUnit.CUBICMETERCOMPENSATED);
        register("watt per watt (W/W)",ReadingTypeUnit.WATTPERWATT);
        register("therm (therm)",ReadingTypeUnit.THERM);
        register("refractive index",ReadingTypeUnit.REFRACTIVEINDEX);
        register("relative permeability",ReadingTypeUnit.RELATIVEPERMEABILITY);
        register("Neper (Np)",ReadingTypeUnit.NEPER);
        register("/m (/m)",ReadingTypeUnit.WAVENUMBER);
        register("cubic meter per kilogram (m\u00b3/kg)",ReadingTypeUnit.CUBICMETERPERKILOGRAM);
        register("pascal second (Pas)",ReadingTypeUnit.PASCALSECOND);
        register("newton meter (Nm)",ReadingTypeUnit.NEWTONMETER);
        register("newton per meter (N/m)",ReadingTypeUnit.NEWTONPERMETER);
        register("radians per second squared (rad/s\u00b2)",ReadingTypeUnit.RADIANSPERSECONDSQUARED);
        register("watt per squared meter (W/m\u000b2)",ReadingTypeUnit.WATTPERSQUAREDMETER);
        register("Joule per kilogram kelvin (J/(kgK))",ReadingTypeUnit.JOULEPERKILOGRAMKELVIN);
        register("Joule per cubic meter (J/m\u00b3)",ReadingTypeUnit.JOULEPERCUBICMETER);
        register("Volt per meter (V/m)",ReadingTypeUnit.VOLTPERMETER);
        register("Coulomb per cubic meter (C/m\u00b3)",ReadingTypeUnit.COULOMBPERCUBICMETER);
        register("Coulomb per square meter (C/m\u00b2)",ReadingTypeUnit.COULOMBPERSQUAREMETER);
        register("Farad per meter (F/m)",ReadingTypeUnit.FARADPERMETER);
        register("Henry per meter (H/m)",ReadingTypeUnit.HENRYPERMETER);
        register("Joule per mole (J/mol)",ReadingTypeUnit.JOULEPERMOLE);
        register("Joule per mole kelvin (J/(molK))",ReadingTypeUnit.JOULEPERMOLEKELVIN);
        register("Coulomb per kilogram (C/kg)",ReadingTypeUnit.COULOMBPERKILOGRAM);
        register("Gray per second (Gy/s)",ReadingTypeUnit.GRAYPERSECOND);
        register("Watt per steradian (W/sr)",ReadingTypeUnit.WATTPERSTERADIAN);
        register("Watt per square meter steradian (W/(m\u00b2sr))",ReadingTypeUnit.WATTPERSQUAREMETERSTERADIAN);
        register("Katal per cubic meter (kat/m\u00b3)",ReadingTypeUnit.KATALPERCUBICMETER);
        register("day (d)",ReadingTypeUnit.DAY);
        register("Angle minute (')",ReadingTypeUnit.ANGLEMIN);
        register("Angle second (\")",ReadingTypeUnit.ANGLESECOND);
        register("hectare (ha)",ReadingTypeUnit.HECTARE);
        register("ton (t)",ReadingTypeUnit.TON);
        register("electronvolt (eV)",ReadingTypeUnit.ELECTRONVOLT);
        register("dalton (Da)",ReadingTypeUnit.DALTON);
        register("unified mass (u)",ReadingTypeUnit.UNIFIEDMASS);
        register("astronomical unit (ua)",ReadingTypeUnit.ASTRONOMICALUNIT);
        register("light speed (c\u2080)",ReadingTypeUnit.LIGHTSPEED);
        register("natural unit of action (\u210f)",ReadingTypeUnit.NATURALACTION);
        register("natural mass (m\u2091)",ReadingTypeUnit.NATURALMASS);
        register("natural unit of time (\u210f/(m\u2091c\u2080\u00b2))",ReadingTypeUnit.NATURALTIME);
        register("electron charge (e)",ReadingTypeUnit.ELECTRONCHARGE);
        register("electron mass (m\u2091)",ReadingTypeUnit.ELECTRONMASS);
        register("atomic unit of action (\u210f)",ReadingTypeUnit.ATOMICACTION);
        register("bohr radius (a\u2080)",ReadingTypeUnit.BOHRRADIUS);
        register("hartree (E\u2095)",ReadingTypeUnit.HARTREE);
        register("atomic unit of time (\u210f/E\u2095)",ReadingTypeUnit.ATOMICTIME);
        register("bar (bar)",ReadingTypeUnit.BAR);
        register("millimeter of mercury (mmHg)",ReadingTypeUnit.MMMERCURY);
        register("angstrom (\u212b)",ReadingTypeUnit.ANGSTROM);
        register("nautical mile (M)",ReadingTypeUnit.NAUTICALMILE);
        register("barn (b)",ReadingTypeUnit.BARN);
        register("knot (kn)",ReadingTypeUnit.KNOT);
        register("curie (Ci)",ReadingTypeUnit.CURIE);
        register("roentgen (R)",ReadingTypeUnit.ROENTGEN);
        register("rad (rd)",ReadingTypeUnit.RAD);
        register("rem (rem)",ReadingTypeUnit.REM);
        register("inch (in)",ReadingTypeUnit.INCH);
        register("foot (ft)",ReadingTypeUnit.FOOT);
        register("rod (rod)",ReadingTypeUnit.ROD);
        register("furlong (fur)",ReadingTypeUnit.FURLONG);
        register("mile (mi)",ReadingTypeUnit.MILE);
        register("square foot (ft\u00b2)",ReadingTypeUnit.SQUAREFOOT);
        register("square yard (yd\u00b2)",ReadingTypeUnit.SQUAREYARD);
        register("square rod (rod\u00b2)",ReadingTypeUnit.SQUAREROD);
        register("acre (acre)",ReadingTypeUnit.ACRE);
        register("square mile (mi\u00b2)",ReadingTypeUnit.SQUAREMILE);
        register("section of land (mi\u00b2)",ReadingTypeUnit.SECTIONOFLAND);
        register("township (township)",ReadingTypeUnit.TOWNSHIP);
        register("cubic yard (yd\u00b3)",ReadingTypeUnit.CUBICYARD);
        register("link (li)",ReadingTypeUnit.LINK);
        register("chain (ch)",ReadingTypeUnit.CHAIN);
        register("us liquid pint (US liq pt)",ReadingTypeUnit.USLIQUIDPINT);
        register("us liquid quart (US liq qt)",ReadingTypeUnit.USLIQUIDQUART);
        register("Apothecaries fluid dram (fl dr ap)",ReadingTypeUnit.FLUIDDREAM);
        register("Apothecaries fluid once (fl oz ap)",ReadingTypeUnit.FLUIDOUNCE);
        register("us dry pint (US dry pt)",ReadingTypeUnit.USDRYPINT);
        register("us dry quart (US dry qt)",ReadingTypeUnit.USDRYQUART);
        register("us peck (US pk)",ReadingTypeUnit.USPECK);
        register("us bushel (Us bushel)",ReadingTypeUnit.USBUSHEL);
        register("grain (gr)",ReadingTypeUnit.GRAIN);
        register("Avoirdupois dram (avdp dr)",ReadingTypeUnit.AVOIRDUPOISDRAM);
        register("Avoirdupois ounce (avdp oz)",ReadingTypeUnit.AVOIRDUPOISONCE);
        register("Avoirdupois pound (avdp lb)",ReadingTypeUnit.AVOIRDUPOISPOUND);
        register("Avoirdupois hundred weight (short cwt)",ReadingTypeUnit.AVOIRDUPOISHUNDREDWEIGHT);
        register("Avoirdupois ton (short ton)",ReadingTypeUnit.AVOIRDUPOISTON);
        register("Avoirdupois gross (long cwt)",ReadingTypeUnit.AVOIRDUPOISGROSS);
        register("Avoirdupois long ton (long ton)",ReadingTypeUnit.AVOIRDUPOISLONGTON);
        register("Troy pennyweight (dwt)",ReadingTypeUnit.TROYPENNYWEIGHT);
        register("Troy ounce (oz t)",ReadingTypeUnit.TROYOUNCE);
        register("Troy pound (lb t)",ReadingTypeUnit.TROYPOUND);
        register("Apothecaries scruple (s ap)",ReadingTypeUnit.APOTHECARIESSCRUPLE);
        register("Apothecaries dram (dr ap)",ReadingTypeUnit.APOTHECARIESDRAM);
        register("Apothecaries ounce (oz ap)",ReadingTypeUnit.APOTHECARIESOUNCE);
        register("Apothecaries pound (lb ap)",ReadingTypeUnit.APOTHECARIESPOUND);
        register("Mile per imperial gallon (mpg (Imp))",ReadingTypeUnit.MILEPERIMPERIALGALLON);
        register("Mile per US gallon (mpg (US))",ReadingTypeUnit.MILEPERUSGALLON);
        register("Mile per US gallon equivalent (MPGe (US))",ReadingTypeUnit.MILEPERUSGALLONEQUIVALENT);
        register("Litre per 100 km (l/(100km))",ReadingTypeUnit.LITREPER100KM);
        register("Watt hour per mile (Wh/mi)",ReadingTypeUnit.WATTHOURPERMILE);
        register("Watt hour per 100 mile (Wh/(100mi))",ReadingTypeUnit.WATTHOURPER100MILE);
        register("Degrees Fahrenheit (\u0b0F)",ReadingTypeUnit.DEGREESFAHRENHEIT);
        register("Volt hour (Vh)",ReadingTypeUnit.VOLTHOUR);
        register("Humidity",ReadingTypeUnit.HUMIDITY);
        register("Skycover", ReadingTypeUnit.SKYCOVER);
    }
}
