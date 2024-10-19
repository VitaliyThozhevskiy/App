/*
 *  This file is part of DroidDrone.
 *
 *  DroidDrone is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DroidDrone is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DroidDrone.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.cameraxvideorecorder.common;

import java.util.ArrayList;
import java.util.List;

public class FcCommon {
    public static final byte MAX_SUPPORTED_RC_CHANNEL_COUNT = 18;

    public static final byte FC_API_COMPATIBILITY_UNKNOWN = 0;
    public static final byte FC_API_COMPATIBILITY_ERROR = 1;
    public static final byte FC_API_COMPATIBILITY_WARNING = 2;
    public static final byte FC_API_COMPATIBILITY_OK = 3;

    // MSP commands
    public static final short MSP_API_VERSION = 1;
    public static final short MSP_FC_VARIANT = 2;
    public static final short MSP_FC_VERSION = 3;
    public static final short MSP_BATTERY_CONFIG = 32;
    public static final short MSP_MIXER_CONFIG = 42;
    public static final short MSP_RX_MAP = 64;
    public static final short MSP_OSD_CONFIG = 84;
    public static final short MSP_VTX_CONFIG = 88;
    public static final short MSP_STATUS = 101;
    public static final short MSP_RAW_GPS = 106;
    public static final short MSP_COMP_GPS = 107;
    public static final short MSP_ATTITUDE = 108;
    public static final short MSP_ALTITUDE = 109;
    public static final short MSP_ANALOG = 110;
    public static final short MSP_BOXNAMES = 116;
    public static final short MSP_BOXIDS = 119;
    public static final short MSP_BATTERY_STATE = 130;
    public static final short MSP_DISPLAYPORT = 182;
    public static final short MSP_OSD_CANVAS = 189;
    public static final short MSP_SET_RAW_RC = 200;

    //autopilot commands

    // commands
    public static final short MSP_START_CAMERA_DETECTION = 201;

    // requests
    public static final short MSP_SET_MISSION_CONFIG = 301;
    public static final short MSP_SET_TARGETS = 302;

    //end autopilot

    public static final short MSP2_INAV_STATUS = 0x2000;
    public static final short MSP2_INAV_ANALOG = 0x2002;
    public static final short MSP2_INAV_MIXER = 0x2010;

    // DD specific telemetry codes
    public static final short DD_TIMERS = 0x4000;
    public static final short DD_VIDEO_RECORDER_START_STOP = 0x4005;

    public static final byte BF_BOXMODES_PAGE_COUNT = 2;



    // all supported API versions here
    public static final ApiSupportedVersion[] API_SUPPORTED_VERSION_INAV = {new ApiSupportedVersion(0, 2, 5)};
    public static final ApiSupportedVersion[] API_SUPPORTED_VERSION_BETAFLIGHT = {new ApiSupportedVersion(0, 1, 45),
            new ApiSupportedVersion(0, 1, 46)/*, new ApiSupportedVersion(0, 1, 47)*/};
    public static final ApiSupportedVersion[] API_SUPPORTED_VERSION_ARDUPILOT = {new ApiSupportedVersion(0, 2, 3)};

    public static class ApiSupportedVersion {
        public final int protocolVersion;
        public final int versionMajor;
        public final int versionMinor;

        public ApiSupportedVersion(int protocolVersion, int versionMajor, int versionMinor) {
            this.protocolVersion = protocolVersion;
            this.versionMajor = versionMajor;
            this.versionMinor = versionMinor;
        }
    }

    public enum BoxModeIds{ // All Inav and Btfl modes. Use for internal logic only. Use box permanentId to match FC.
        BOXARM,
        BOXANGLE,
        BOXHORIZON,
        BOXMAG,
        BOXHEADFREE,
        BOXPASSTHRU,
        BOXHEADADJ,
        BOXCAMSTAB,
        BOXGPSRESCUE,
        BOXANTIGRAVITY,
        BOXBEEPERON,
        BOXLEDLOW,
        BOXCALIB,
        BOXOSD,
        BOXTELEMETRY,
        BOXSERVO1,
        BOXSERVO2,
        BOXSERVO3,
        BOXBLACKBOX,
        BOXFAILSAFE,
        BOXAIRMODE,
        BOX3D,
        BOXCAMERA1,
        BOXCAMERA2,
        BOXCAMERA3,
        BOXFLIPOVERAFTERCRASH,
        BOXUSER1,
        BOXUSER2,
        BOXUSER3,
        BOXUSER4,
        BOXPIDAUDIO,
        BOXACROTRAINER,
        BOXVTXCONTROLDISABLE,
        BOXLAUNCHCONTROL,
        BOXMSPOVERRIDE,
        BOXSTICKCOMMANDDISABLE,
        BOXREADY,
        BOXLAPTIMERRESET,
        BOXFPVANGLEMIX,
        BOXBLACKBOXERASE,
        BOXPREARM,
        BOXBEEPGPSCOUNT,
        BOXVTXPITMODE,
        BOXPARALYZE,
        BOXBEEPERMUTE
    }

    public static class BoxMode{
        public final BoxModeIds boxId;
        public final String boxName;
        public final int permanentId;
        public final int osdPriority;

        public BoxMode(BoxModeIds boxId, String boxName, int permanentId, int osdPriority) {
            this.boxId = boxId;
            this.boxName = boxName;
            this.permanentId = permanentId;
            this.osdPriority = osdPriority;
        }
    }

    public static final BoxMode[] boxModesBtfl = {
            new BoxMode(BoxModeIds.BOXARM,                  "ARM",                      0,  1),
            new BoxMode(BoxModeIds.BOXANGLE,                "ANGLE",                    1,  5),
            new BoxMode(BoxModeIds.BOXHORIZON,              "HORIZON",                  2,  10),
            new BoxMode(BoxModeIds.BOXANTIGRAVITY,          "ANTI GRAVITY",             4,  0),
            new BoxMode(BoxModeIds.BOXMAG,                  "MAG",                      5,  15),
            new BoxMode(BoxModeIds.BOXHEADFREE,             "HEADFREE",                 6,  10),
            new BoxMode(BoxModeIds.BOXHEADADJ,              "HEADADJ",                  7,  0),
            new BoxMode(BoxModeIds.BOXCAMSTAB,              "CAMSTAB",                  8,  0),
            new BoxMode(BoxModeIds.BOXPASSTHRU,             "PASSTHRU",                 12, 0),
            new BoxMode(BoxModeIds.BOXBEEPERON,             "BEEPER",                   13, 0),
            new BoxMode(BoxModeIds.BOXLEDLOW,               "LEDLOW",                   15, 0),
            new BoxMode(BoxModeIds.BOXCALIB,                "CALIB",                    17, 0),
            new BoxMode(BoxModeIds.BOXOSD,                  "OSD DISABLE",              19, 0),
            new BoxMode(BoxModeIds.BOXTELEMETRY,            "TELEMETRY",                20, 0),
            new BoxMode(BoxModeIds.BOXSERVO1,               "SERVO1",                   23, 0),
            new BoxMode(BoxModeIds.BOXSERVO2,               "SERVO2",                   24, 0),
            new BoxMode(BoxModeIds.BOXSERVO3,               "SERVO3",                   25, 0),
            new BoxMode(BoxModeIds.BOXBLACKBOX,             "BLACKBOX",                 26, 0),
            new BoxMode(BoxModeIds.BOXFAILSAFE,             "FAILSAFE",                 27, 90),
            new BoxMode(BoxModeIds.BOXAIRMODE,              "AIR MODE",                 28, 2),
            new BoxMode(BoxModeIds.BOX3D,                   "3D DISABLE / SWITCH",      29, 0),
            new BoxMode(BoxModeIds.BOXFPVANGLEMIX,          "FPV ANGLE MIX",            30, 0),
            new BoxMode(BoxModeIds.BOXBLACKBOXERASE,        "BLACKBOX ERASE",           31, 0),
            new BoxMode(BoxModeIds.BOXCAMERA1,              "CAMERA CONTROL 1",         32, 0),
            new BoxMode(BoxModeIds.BOXCAMERA2,              "CAMERA CONTROL 2",         33, 0),
            new BoxMode(BoxModeIds.BOXCAMERA3,              "CAMERA CONTROL 3",         34, 0),
            new BoxMode(BoxModeIds.BOXFLIPOVERAFTERCRASH,   "FLIP OVER AFTER CRASH",    35, 0),
            new BoxMode(BoxModeIds.BOXPREARM,               "PREARM",                   36, 2),
            new BoxMode(BoxModeIds.BOXBEEPGPSCOUNT,         "GPS BEEP SATELLITE COUNT", 37, 0),
            new BoxMode(BoxModeIds.BOXVTXPITMODE,           "VTX PIT MODE",             39, 0),
            new BoxMode(BoxModeIds.BOXUSER1,                "USER1",                    40, 0),
            new BoxMode(BoxModeIds.BOXUSER2,                "USER2",                    41, 0),
            new BoxMode(BoxModeIds.BOXUSER3,                "USER3",                    42, 0),
            new BoxMode(BoxModeIds.BOXUSER4,                "USER4",                    43, 0),
            new BoxMode(BoxModeIds.BOXPIDAUDIO,             "PID AUDIO",                44, 0),
            new BoxMode(BoxModeIds.BOXPARALYZE,             "PARALYZE",                 45, 5),
            new BoxMode(BoxModeIds.BOXGPSRESCUE,            "GPS RESCUE",               46, 100),
            new BoxMode(BoxModeIds.BOXACROTRAINER,          "ACRO TRAINER",             47, 0),
            new BoxMode(BoxModeIds.BOXVTXCONTROLDISABLE,    "VTX CONTROL DISABLE",      48, 0),
            new BoxMode(BoxModeIds.BOXLAUNCHCONTROL,        "LAUNCH CONTROL",           49, 3),
            new BoxMode(BoxModeIds.BOXMSPOVERRIDE,          "MSP OVERRIDE",             50, 10),
            new BoxMode(BoxModeIds.BOXSTICKCOMMANDDISABLE,  "STICK COMMANDS DISABLE",   51, 0),
            new BoxMode(BoxModeIds.BOXBEEPERMUTE,           "BEEPER MUTE",              52, 0),
            new BoxMode(BoxModeIds.BOXREADY,                "READY",                    53, 0),
            new BoxMode(BoxModeIds.BOXLAPTIMERRESET,        "LAP TIMER RESET",          54, 0),
    };

    public static class PlatformTypesBtfl{
        public static final int MIXER_TRI = 1;
        public static final int MIXER_QUADP = 2;
        public static final int MIXER_QUADX = 3;
        public static final int MIXER_BICOPTER = 4;
        public static final int MIXER_Y6 = 6;
        public static final int MIXER_HEX6 = 7;
        public static final int MIXER_FLYING_WING = 8;
        public static final int MIXER_Y4 = 9;
        public static final int MIXER_HEX6X = 10;
        public static final int MIXER_OCTOX8 = 11;
        public static final int MIXER_OCTOFLATP = 12;
        public static final int MIXER_OCTOFLATX = 13;
        public static final int MIXER_AIRPLANE = 14;
        public static final int MIXER_HELI_120_CCPM = 15;
        public static final int MIXER_HELI_90_DEG = 16;
        public static final int MIXER_VTAIL4 = 17;
        public static final int MIXER_HEX6H = 18;
        public static final int MIXER_DUALCOPTER = 20;
        public static final int MIXER_SINGLECOPTER = 21;
        public static final int MIXER_ATAIL4 = 22;
        public static final int MIXER_CUSTOM = 23;
        public static final int MIXER_CUSTOM_AIRPLANE = 24;
        public static final int MIXER_CUSTOM_TRI = 25;
        public static final int MIXER_QUADX_1234 = 26;
        public static final int MIXER_OCTOX8P = 27;

        public static String getPlatforTypeName(int platformType){
            switch (platformType){
                case MIXER_TRI:
                    return "Tricopter";
                case MIXER_QUADP:
                    return "QuadP";
                case MIXER_QUADX:
                    return "QuadX";
                case MIXER_BICOPTER:
                    return "Bicopter";
                case MIXER_Y6:
                    return "Y6";
                case MIXER_HEX6:
                    return "Hex6";
                case MIXER_FLYING_WING:
                    return "Flying Wing";
                case MIXER_Y4:
                    return "Y4";
                case MIXER_HEX6X:
                    return "Hex6X";
                case MIXER_OCTOX8:
                    return "OctoX8";
                case MIXER_OCTOFLATP:
                    return "OctoFlatP";
                case MIXER_OCTOFLATX:
                    return "OctoFlatX";
                case MIXER_AIRPLANE:
                    return "Airplane";
                case MIXER_HELI_120_CCPM:
                    return "Heli120CCPM";
                case MIXER_HELI_90_DEG:
                    return "Heli90Deg";
                case MIXER_VTAIL4:
                    return "VTail4";
                case MIXER_HEX6H:
                    return "Hex6H";
                case MIXER_DUALCOPTER:
                    return "Dualcopter";
                case MIXER_SINGLECOPTER:
                    return "Singlecopter";
                case MIXER_ATAIL4:
                    return "ATail4";
                case MIXER_CUSTOM:
                    return "Custom";
                case MIXER_CUSTOM_AIRPLANE:
                    return "Custom Airplane";
                case MIXER_CUSTOM_TRI:
                    return "Custom Tricopter";
                case MIXER_QUADX_1234:
                    return "QuadX1234";
                case MIXER_OCTOX8P:
                    return "OctoX8P";
                default:
                    return "N/A";
            }
        }
    }

    public static int[] getBoxIds(byte[] data){
        if (data == null || data.length == 0) return null;
        int[] boxIds = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            boxIds[i] = data[i] & 0xFF;
        }
        return boxIds;
    }

    public static BoxMode[] getActiveBoxesBtfl(byte[] flags, int[] boxIds){
        if (boxIds == null || flags == null) return null;
        List<BoxMode> activeBoxes = new ArrayList<>();
        for (int i = 0; i < boxIds.length; i++) {
            if (((flags[(i / 8)] >> i % 8) & 1) == 1) {
                int permanentId = boxIds[i];
                for (FcCommon.BoxMode box : boxModesBtfl) {
                    if (box.permanentId == permanentId) {
                        activeBoxes.add(box);
                        break;
                    }
                }
            }
        }
        return activeBoxes.toArray(new FcCommon.BoxMode[0]);
    }

    public static int getFcApiCompatibilityLevel(FcInfo fcInfo) {
        int compatibilityLevel = FcCommon.FC_API_COMPATIBILITY_UNKNOWN;
        if (fcInfo == null) return compatibilityLevel;
        FcCommon.ApiSupportedVersion[] supportedVersions;
        switch (fcInfo.getFcVariant()) {
            case FcInfo.FC_VARIANT_BETAFLIGHT:
                supportedVersions = FcCommon.API_SUPPORTED_VERSION_BETAFLIGHT;
                break;
            default:
                return compatibilityLevel;
        }
        for (FcCommon.ApiSupportedVersion supportedVersion : supportedVersions) {
            if (fcInfo.getApiProtocolVersion() == supportedVersion.protocolVersion) {
                if (fcInfo.getApiVersionMajor() == supportedVersion.versionMajor) {
                    if (fcInfo.getApiVersionMinor() == supportedVersion.versionMinor) {
                        return FcCommon.FC_API_COMPATIBILITY_OK;
                    }else{
                        compatibilityLevel = FcCommon.FC_API_COMPATIBILITY_WARNING;
                    }
                }else{
                    if (compatibilityLevel < FcCommon.FC_API_COMPATIBILITY_ERROR) compatibilityLevel = FcCommon.FC_API_COMPATIBILITY_ERROR;
                }
            }else{
                if (compatibilityLevel < FcCommon.FC_API_COMPATIBILITY_ERROR) compatibilityLevel = FcCommon.FC_API_COMPATIBILITY_ERROR;
            }
        }
        return compatibilityLevel;
    }
}