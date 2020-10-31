package com.phannhatquang.trackme.ui.maps.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.se.omapi.Session;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.phannhatquang.trackme.R;
import com.phannhatquang.trackme.TrackMeApplication;
import com.phannhatquang.trackme.adapter.HistoryAdapter;
import com.phannhatquang.trackme.data.dao.SessionDAO;
import com.phannhatquang.trackme.data.model.MyLocation;
import com.phannhatquang.trackme.data.model.TableSession;
import com.phannhatquang.trackme.utils.AppState;
import com.phannhatquang.trackme.utils.PermissionUtils;
import com.phannhatquang.trackme.utils.SharePref_;
import com.phannhatquang.trackme.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

@EActivity(R.layout.main_activity)
public class MainActivity extends AppCompatActivity {

    @Pref
    SharePref_ mSharePref;

    @ViewById(R.id.rvHistory)
    RecyclerView rvHistory;

    List<TableSession> sessions;

    HistoryAdapter historyAdapter;


    @AfterViews
    protected void afterViews() {
        new GetDataBaseTask().execute();
    }


    private void setupRecycleView() {

        // set up the RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(this, sessions);
        historyAdapter.setClickListener(new HistoryAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MapsActivity_.intent(MainActivity.this).isHistory(true).historySessionID(sessions.get(position).id).start();
            }
        });
        rvHistory.setAdapter(historyAdapter);
    }


    public class GetDataBaseTask<T> extends AsyncTask<Object, Void, T> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected T doInBackground(Object... objects) {
            List<TableSession> tableSessions = TrackMeApplication.getInstance()
                    .getAppDatabase().sessionDAO().getAll();
            Log.d("________", tableSessions.size() + "");
            sessions = tableSessions;
            return null;
        }

        @Override
        protected void onPostExecute(T result) {
            if (sessions == null || sessions.size() <= 0
                    || mSharePref.currentState().getOr(-1) == AppState.RUNNING) {
                MapsActivity_.intent(MainActivity.this).isHistory(false).start();
            } else {
                setupRecycleView();
            }
        }
    }


    @Click(R.id.tvClear)
    void onClearHistory() {
        new DeleteAllHistoryTask().execute();

    }

    void notifyDataChange() {
        historyAdapter.notifyDataSetChanged();
    }


    public class DeleteAllHistoryTask<T> extends AsyncTask<Object, Void, T> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected T doInBackground(Object... objects) {
            for (final TableSession session : sessions) {
                TrackMeApplication.getInstance()
                        .getAppDatabase().sessionDAO().delete(session);
            }
            sessions = TrackMeApplication.getInstance()
                    .getAppDatabase().sessionDAO().getAll();
            return null;
        }

        @Override
        protected void onPostExecute(T result) {
            notifyDataChange();
        }
    }

}
