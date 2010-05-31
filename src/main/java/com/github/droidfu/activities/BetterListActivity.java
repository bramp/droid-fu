/* Copyright (c) 2009 Matthias Käppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.droidfu.activities;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.ListAdapter;

import com.github.droidfu.R;
import com.github.droidfu.DroidFuApplication;
import com.github.droidfu.adapters.ListAdapterWithProgress;
import com.github.droidfu.dialogs.DialogClickListener;

public class BetterListActivity extends ListActivity implements BetterActivity {

    private static final String IS_BUSY_EXTRA = "is_busy";

    private boolean wasCreated, wasInterrupted;

    // TODO Save/Restore these variables
    private CharSequence progressDialogTitle;
    private CharSequence progressDialogMsg;

    private Intent currentIntent;

    /**
     * The Dialog currently being displayed
     */
    private WeakReference<Dialog> dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.wasCreated = true;
        this.currentIntent = getIntent();

        ((DroidFuApplication) getApplication()).setActiveContext(getClass().getCanonicalName(),
            this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ((DroidFuApplication)
        // getApplication()).resetActiveContext(getClass().getCanonicalName());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ListAdapter adapter = getListAdapter();
        if (adapter instanceof ListAdapterWithProgress<?>) {
            boolean isLoading = ((ListAdapterWithProgress<?>) adapter).isLoadingData();
            outState.putBoolean(IS_BUSY_EXTRA, isLoading);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ListAdapter adapter = getListAdapter();
        if (adapter instanceof ListAdapterWithProgress<?>) {
            boolean isLoading = savedInstanceState.getBoolean(IS_BUSY_EXTRA);
            ((ListAdapterWithProgress<?>) adapter).setIsLoadingData(isLoading);
        }
        wasInterrupted = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasCreated = wasInterrupted = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.currentIntent = intent;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_PROGRESS)
            return BetterActivityHelper.newProgressDialog(this, progressDialogTitle, progressDialogMsg);
        return null;
    }
/*
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        // Store a weak reference to this dialog. It is not a strong reference
        // because we are unsure when the dialog is finally dismissed, thus we
        // might hold onto a old reference. So instead this way we still let
        // the garbage collector run.
        this.dialog = new WeakReference<Dialog>(dialog);

        // Ensure the title/message is current
        if (id == DIALOG_PROGRESS) {
            ProgressDialog progressDialog = (ProgressDialog) dialog;
            if (progressDialogTitle != null) {
                progressDialog.setTitle(progressDialogTitle);
            }

            progressDialog.setMessage(progressDialogMsg);
        }
    }
*/
    public Dialog getCurrentDialog() {
        final Dialog d = dialog != null ? dialog.get() : null;
        if (d != null && d.isShowing())
            return d;
        return null;
    }

    public void setProgressDialogTitle(CharSequence progressDialogTitle) {
        this.progressDialogTitle = progressDialogTitle;

        final Dialog dialog = getCurrentDialog();
        if (dialog != null && dialog instanceof ProgressDialog)
            dialog.setTitle(progressDialogTitle);
    }

    public void setProgressDialogMsg(CharSequence progressDialogMsg) {
        this.progressDialogMsg = progressDialogMsg;

        final Dialog dialog = getCurrentDialog();
        if (dialog != null && dialog instanceof ProgressDialog)
            ((ProgressDialog)dialog).setMessage(progressDialogMsg);
    }

    public int getWindowFeatures() {
        return BetterActivityHelper.getWindowFeatures(this);
    }

    public boolean isRestoring() {
        return wasInterrupted;
    }

    public boolean isResuming() {
        return !wasCreated;
    }

    public boolean isLaunching() {
        return !wasInterrupted && wasCreated;
    }

    public boolean isApplicationBroughtToBackground() {
        return BetterActivityHelper.isApplicationBroughtToBackground(this);
    }

    public Intent getCurrentIntent() {
        return currentIntent;
    }

    public boolean isLandscapeMode() {
        return getWindowManager().getDefaultDisplay().getOrientation() == 1;
    }

    public boolean isPortraitMode() {
        return !isLandscapeMode();
    }

    public AlertDialog newYesNoDialog(int titleResourceId, int messageResourceId,
            OnClickListener listener) {
        return BetterActivityHelper.newYesNoDialog(this, getString(titleResourceId),
            getString(messageResourceId), android.R.drawable.ic_dialog_info, listener);
    }

    public AlertDialog newInfoDialog(int titleResourceId, int messageResourceId) {
        return BetterActivityHelper.newMessageDialog(this, getString(titleResourceId),
            getString(messageResourceId), android.R.drawable.ic_dialog_info);
    }

    public AlertDialog newAlertDialog(int titleResourceId, int messageResourceId) {
        return BetterActivityHelper.newMessageDialog(this, getString(titleResourceId),
            getString(messageResourceId), android.R.drawable.ic_dialog_alert);
    }

    public AlertDialog newErrorHandlerDialog(int titleResourceId, Exception error) {
        return BetterActivityHelper.newErrorHandlerDialog(this, getString(titleResourceId), error);
    }

    public AlertDialog newErrorHandlerDialog(Exception error) {
        return newErrorHandlerDialog(R.string.droidfu_error_dialog_title, error);
    }

    public <T> Dialog newListDialog(List<T> elements, DialogClickListener<T> listener,
            boolean closeOnSelect) {
        return BetterActivityHelper.newListDialog(this, elements, listener, closeOnSelect);
    }
}
