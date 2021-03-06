/*
 * (C) Copyright 2010-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 */

package org.nuxeo.android.simpleclient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.nuxeo.android.simpleclient.menus.LoginScreenActivity;
import org.nuxeo.android.simpleclient.menus.SettingsActivity;
import org.nuxeo.android.simpleclient.service.NuxeoAndroidServices;
import org.nuxeo.android.simpleclient.ui.TitleBarAggregate;
import org.nuxeo.android.simpleclient.ui.TitleBarAttributes;
import org.nuxeo.android.simpleclient.ui.TitleBarDiscarded;
import org.nuxeo.android.simpleclient.ui.TitleBarRefreshFeature;
import org.nuxeo.android.simpleclient.ui.TitleBarShowHomeFeature;
import org.nuxeo.android.simpleclient.ui.TitleBarShowSearchFeature;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.AndroidRuntimeException;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.smartnsoft.droid4me.app.ActivityController;
import com.smartnsoft.droid4me.app.AppPublics;
import com.smartnsoft.droid4me.app.SmartApplication;
import com.smartnsoft.droid4me.bo.Business.InputAtom;
import com.smartnsoft.droid4me.cache.DbPersistence;
import com.smartnsoft.droid4me.cache.Persistence;
import com.smartnsoft.droid4me.download.AdvancedImageDownloader;
import com.smartnsoft.droid4me.download.BasisImageDownloader.InputStreamDownloadInstructor;
import com.smartnsoft.droid4me.download.ImageDownloader;

/**
 * The entry point of the application.
 *
 * @author Nuxeo & Smart&Soft
 * @since 2011.02.17
 */
public final class NuxeoAndroidApplication extends SmartApplication {

    @Override
    protected int getLogLevel() {
        return Constants.LOG_LEVEL;
    }

    @Override
    protected SmartApplication.I18N getI18N() {
        return new SmartApplication.I18N(getText(R.string.problem),
                getText(R.string.unavailableItem),
                getText(R.string.unavailableService),
                getText(R.string.connectivityProblem),
                getText(R.string.unhandledProblem));
    }

    @Override
    protected SmartApplication.I18NExt getI18NExt() {
        return new SmartApplication.I18NExt(
                getString(R.string.applicationName),
                getText(R.string.dialogButton_unhandledProblem),
                getString(R.string.progressDialogMessage_unhandledProblem));
    }

    public static class CacheInstructions extends
            AdvancedImageDownloader.AdvancedAbstractInstructions {

        @Override
        public final InputStream getInputStream(String imageUid,
                Object imageSpecs, String url,
                InputStreamDownloadInstructor arg3) throws IOException {
            final InputAtom inputAtom = Persistence.getInstance(1).extractInputStream(
                    url);
            return inputAtom == null ? null : inputAtom.inputStream;
        }

        @Override
        public InputStream onInputStreamDownloaded(String imageUid,
                Object imageSpecs, String url, InputStream inputStream) {
            return Persistence.getInstance(1).flushInputStream(url,
                    new InputAtom(new Date(), inputStream)).inputStream;
        }

        @Override
        public boolean onBindImage(boolean downloaded, ImageView imageView,
                Bitmap bitmap, String imageUid, Object imageSpecs) {
            imageView.setVisibility(View.VISIBLE);
            return super.onBindImage(downloaded, imageView, bitmap, imageUid,
                    imageSpecs);
        }

    }

    public final static ImageDownloader.Instructions CACHE_IMAGE_INSTRUCTIONS = new NuxeoAndroidApplication.CacheInstructions();

    @Override
    protected String getLogReportRecipient() {
        return Constants.REPORT_LOG_RECIPIENT_EMAIL;
    }

    @Override
    public void onCreateCustom() {
        super.onCreateCustom();

        // We initialize the persistence
        final String directoryName = getPackageManager().getApplicationLabel(
                getApplicationInfo()).toString();
        final File contentsDirectory = new File(
                Environment.getExternalStorageDirectory(), directoryName);
        Persistence.CACHE_DIRECTORY_PATHS = new String[] {
                contentsDirectory.getAbsolutePath(),
                contentsDirectory.getAbsolutePath() };
        DbPersistence.FILE_NAMES = new String[] {
                DbPersistence.DEFAULT_FILE_NAME,
                DbPersistence.DEFAULT_FILE_NAME };
        DbPersistence.TABLE_NAMES = new String[] { "data", "images" };
        Persistence.CACHES_COUNT = 2;
        Persistence.IMPLEMENTATION_FQN = DbPersistence.class.getName();

        // We set the ImageDownloader instances
        ImageDownloader.IMPLEMENTATION_FQN = AdvancedImageDownloader.class.getName();
        ImageDownloader.INSTANCES_COUNT = 1;
        ImageDownloader.MAX_MEMORY_IN_BYTES = new long[] { 3 * 1024 * 1024 };
        ImageDownloader.LOW_LEVEL_MEMORY_WATER_MARK_IN_BYTES = new long[] { 1 * 1024 * 1024 };
        ImageDownloader.USE_REFERENCES = new boolean[] { false };
        ImageDownloader.RECYCLE_BITMAP = new boolean[] { false };

        NuxeoAndroidServices.init(getApplicationContext());
    }

    @Override
    protected ActivityController.Redirector getActivityRedirector() {
        return new ActivityController.Redirector() {
            public Intent getRedirection(Activity activity) {
                if (NuxeoAndroidSplashScreenActivity.isInitialized(NuxeoAndroidSplashScreenActivity.class) == false) {
                    // We re-direct to the splash screen activity if the
                    // application has not been yet initialized
                    if (activity.getComponentName() == null
                            || activity.getComponentName().getClassName().equals(
                                    NuxeoAndroidSplashScreenActivity.class.getName()) == true) {
                        return null;
                    } else {
                        return new Intent(activity,
                                NuxeoAndroidSplashScreenActivity.class);
                    }
                }
                // redirect to settings screen if prefs are not set
                if (getPreferences().getString(SettingsActivity.PREF_PASSWORD,
                        null) == null) {
                    if (activity.getComponentName() == null
                            || activity.getComponentName().getClassName().equals(
                                    LoginScreenActivity.class.getName()) == true) {
                        return null;
                    } else {
                        return new Intent(activity, LoginScreenActivity.class);
                    }
                }
                return null;
            }
        };
    }

    @Override
    protected ActivityController.Interceptor getActivityInterceptor() {
        return new ActivityController.Interceptor() {
            public void onLifeCycleEvent(Activity activity,
                    ActivityController.Interceptor.InterceptorEvent event) {
                if (event == ActivityController.Interceptor.InterceptorEvent.onSuperCreateBefore
                        && !(activity instanceof TitleBarDiscarded)) {
                    if (activity.getParent() == null
                            && activity instanceof AppPublics.CommonActivity<?>) {
                        boolean requestWindowFeature;
                        try {
                            activity.setTheme(R.style.Theme_NuxeoAndroid);
                            requestWindowFeature = activity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
                        } catch (AndroidRuntimeException exception) {
                            // This means that the activity does not support
                            // custom titles
                            return;
                        }
                        // We test whether we can customize the title bar
                        final TitleBarAggregate titleBarAggregate = new TitleBarAggregate(
                                activity, requestWindowFeature);
                        final AppPublics.CommonActivity<TitleBarAggregate> commonActivity = (AppPublics.CommonActivity<TitleBarAggregate>) activity;
                        commonActivity.setAggregate(titleBarAggregate);
                    }
                } else if (event == ActivityController.Interceptor.InterceptorEvent.onContentChanged
                        && !(activity instanceof TitleBarDiscarded)) {
                    if (activity.getParent() == null
                            && activity instanceof AppPublics.CommonActivity<?>) {
                        final AppPublics.CommonActivity<TitleBarAggregate> commonActivity = (AppPublics.CommonActivity<TitleBarAggregate>) activity;
                        final TitleBarAggregate titleBarAggregate = commonActivity.getAggregate();
                        if (titleBarAggregate != null
                                && titleBarAggregate.customTitleSupported == true
                                && titleBarAggregate.attributes == null) {
                            activity.getWindow().setFeatureInt(
                                    Window.FEATURE_CUSTOM_TITLE,
                                    R.layout.title_bar);
                            titleBarAggregate.attributes = new TitleBarAttributes(
                                    activity,
                                    activity.findViewById(R.id.titleBar));
                            if (activity instanceof TitleBarRefreshFeature) {
                                titleBarAggregate.setOnRefresh((TitleBarRefreshFeature) activity);
                            } else {
                                titleBarAggregate.attributes.setShowRefresh(null);
                            }
                            if (activity instanceof TitleBarShowHomeFeature) {
                                titleBarAggregate.attributes.setShowHome(
                                        R.drawable.ic_title_home,
                                        titleBarAggregate);
                            }
                            if (activity instanceof TitleBarShowSearchFeature) {
                                titleBarAggregate.setOnSearch((TitleBarShowSearchFeature) activity);
                            } else {
                                titleBarAggregate.attributes.setShowSearch(
                                        false, null);
                            }

                            // We register the receivers
                            commonActivity.registerBroadcastListeners(new AppPublics.BroadcastListener[] { titleBarAggregate });
                        }
                    }
                }
            }
        };
    }

    @Override
    protected ActivityController.ExceptionHandler getExceptionHandler() {
        return super.getExceptionHandler();
    }

}
