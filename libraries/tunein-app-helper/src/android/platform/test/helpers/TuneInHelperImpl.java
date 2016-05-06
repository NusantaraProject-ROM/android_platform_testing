/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.platform.test.helpers;

import android.app.Instrumentation;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.Until;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;
import junit.framework.Assert;

public class TuneInHelperImpl extends AbstractTuneInHelper {
    private static final String TAG = TuneInHelperImpl.class.getCanonicalName();

    private static final String UI_PACKAGE_NAME = "tunein.player";
    private static final long UI_ACTION_TIMEOUT = 5000;
    private static final int MAX_BACK_ATTEMPTS = 5;

    private static final String UI_LOCAL_RADIO_TEXT = "Local Radio";
    private static final String UI_FM_LIST_ID = "view_model_list";
    private static final String UI_START_PLAY_ID = "profile_primary_button";
    private static final String UI_MINI_PLAYER_PLAY_ID = "mini_player_play";
    private static final String UI_MINI_PLAYER_STOP_ID = "mini_player_stop";

    public TuneInHelperImpl(Instrumentation instr) {
        super(instr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackage() {
        return UI_PACKAGE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLauncherName() {
        return "TuneIn Radio";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissInitialDialogs() {

    }

    private boolean isOnBrowsePage() {
        return mDevice.hasObject(By.text("Browse"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToBrowsePage() {
        for (int tries = MAX_BACK_ATTEMPTS; tries > 0; tries--) {
            if (isOnBrowsePage()) {
                break;
            }
            mDevice.pressBack();
            mDevice.waitForIdle();
        }
        Assert.assertTrue("Fail to go back to Browse Page", isOnBrowsePage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goToLocalRadio() {
        Assert.assertTrue("Not on Browse Page", isOnBrowsePage());

        UiObject2 localRadio = mDevice.findObject(By.text(UI_LOCAL_RADIO_TEXT));

        Assert.assertNotNull("Could not find local radio item", localRadio);
        Assert.assertTrue(
            "Fail to load Local Radio page",
            localRadio.clickAndWait(Until.newWindow(), UI_ACTION_TIMEOUT)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectFM(int i) {
        UiObject2 fmList = mDevice.wait(
              Until.findObject(By.res(UI_PACKAGE_NAME, UI_FM_LIST_ID)),
              UI_ACTION_TIMEOUT
            );

        Assert.assertNotNull("Could not find fm list", fmList);
        Assert.assertTrue("Invalid ith fm to select",
                          i > 0 && i < fmList.getChildren().size());

        UiObject2 fm = fmList.getChildren().get(i);

        Assert.assertTrue(
            "Fail to load into fm profile page",
            fm.clickAndWait(Until.newWindow(), UI_ACTION_TIMEOUT)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startChannel() {
        UiObject2 start = mDevice
            .findObject(By.res(UI_PACKAGE_NAME, UI_START_PLAY_ID));

        Assert.assertNotNull("Could not find start play button", start);
        Assert.assertTrue(
            "Fail to start playing the fm",
            start.clickAndWait(Until.newWindow(), UI_ACTION_TIMEOUT)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopChannel() {
        UiObject2 stop = mDevice
            .findObject(By.res(UI_PACKAGE_NAME, UI_MINI_PLAYER_STOP_ID));
        Assert.assertNotNull("Could not find stop button", stop);
        stop.click();
        Assert.assertTrue(
            "Fail to stop playing the fm",
            stop.wait(Until.enabled(!stop.isEnabled()), UI_ACTION_TIMEOUT)
        );
    }

}
