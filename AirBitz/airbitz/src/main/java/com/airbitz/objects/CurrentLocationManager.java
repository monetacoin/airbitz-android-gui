/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms are permitted provided that 
 * the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Airbitz Project.
 */

package com.airbitz.objects;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class CurrentLocationManager {

    private static CurrentLocationManager mInstance = null;

    private Context mContext;

    private PlayLocationManager mPlay;
    private AndroidLocationManager mAndroid;

    public CurrentLocationManager(Context context) {
        mContext = context;
        if (supportsPlayServices(context)) {
            mPlay = new PlayLocationManager(mContext);
        } else {
            mAndroid = new AndroidLocationManager(mContext);
        }
    }

    public static boolean supportsPlayServices(Context context) {
        return ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
    }

    public static CurrentLocationManager getLocationManager(Context context) {
        if (null == mInstance) {
            mInstance = new CurrentLocationManager(context);
        }
        return mInstance;
    }

    public void addLocationChangeListener(OnCurrentLocationChange listener) {
        if (mPlay != null) {
            mPlay.addLocationChangeListener(listener);
        } else {
            mAndroid.addLocationChangeListener(listener);
        }
    }

    public void removeLocationChangeListener(OnCurrentLocationChange listener) {
        if (mPlay != null) {
            mPlay.removeLocationChangeListener(listener);
        } else {
            mAndroid.removeLocationChangeListener(listener);
        }
    }

    public Location getLocation() {
        if (mPlay != null) {
            return mPlay.getLocation();
        } else {
            return mAndroid.getLocation();
        }
    }

    public void attemptConnection() {
        if (mPlay != null) {
            mPlay.attemptConnection();
        } else {
            mAndroid.attemptConnection();
        }
    }

    public void onConnected(Bundle bundle) {
        if (mPlay != null) {
            mPlay.onConnected(bundle);
        } else {
            mAndroid.onConnected(bundle);
        }
    }

    public void onDisconnected() {
        if (mPlay != null) {
            mPlay.onDisconnected();
        } else {
            mAndroid.onDisconnected();
        }
    }

    public void onLocationChanged(Location location) {
        if (mPlay != null) {
            mPlay.onLocationChanged(location);
        } else {
            mAndroid.onLocationChanged(location);
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("TAG_LOC", "Connection to LocationClient failed");
    }

    public interface OnCurrentLocationChange {
        public void OnCurrentLocationChange(Location location);
    }
}
