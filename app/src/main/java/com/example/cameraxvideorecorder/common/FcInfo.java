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

public class FcInfo {
    public static final byte FC_VARIANT_UNKNOWN = 0;
    public static final byte FC_VARIANT_BETAFLIGHT = 1;
    public static final String BETAFLIGHT_ID = "BTFL";
    public static final String BETAFLIGHT_NAME = "Betaflight";
    private final int fcVariant;
    private final int fcVersionMajor;
    private final int fcVersionMinor;
    private final int fcVersionPatchLevel;
    private final int apiProtocolVersion;
    private final int apiVersionMajor;
    private final int apiVersionMinor;
    private final int platformType;

    public FcInfo(int fcVariant, int fcVersionMajor, int fcVersionMinor, int fcVersionPatchLevel, int apiProtocolVersion, int apiVersionMajor, int apiVersionMinor, int platformType) {
        this.fcVariant = fcVariant;
        this.fcVersionMajor = fcVersionMajor;
        this.fcVersionMinor = fcVersionMinor;
        this.fcVersionPatchLevel = fcVersionPatchLevel;
        this.apiProtocolVersion = apiProtocolVersion;
        this.apiVersionMajor = apiVersionMajor;
        this.apiVersionMinor = apiVersionMinor;
        this.platformType = platformType;
    }

    public String getFcVersionStr(){
        return fcVersionMajor + "." + fcVersionMinor + "." + fcVersionPatchLevel;
    }

    public String getFcApiVersionStr(){
        return apiProtocolVersion + "." + apiVersionMajor + "." + apiVersionMinor;
    }

    public String getFcName(){
        return BETAFLIGHT_NAME;
    }

    public int getFcVariant() {
        return fcVariant;
    }

    public int getApiProtocolVersion() {
        return apiProtocolVersion;
    }

    public int getApiVersionMajor() {
        return apiVersionMajor;
    }

    public int getApiVersionMinor() {
        return apiVersionMinor;
    }

    public String getPlatformTypeName(){
        switch (fcVariant){
            case FC_VARIANT_BETAFLIGHT:
                return FcCommon.PlatformTypesBtfl.getPlatforTypeName(platformType);
        }
        return null;
    }
}
