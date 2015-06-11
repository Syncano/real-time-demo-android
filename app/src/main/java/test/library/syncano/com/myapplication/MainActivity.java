package test.library.syncano.com.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.syncano.library.SyncServer;
import com.syncano.library.SyncServerListener;
import com.syncano.library.Syncano;
import com.syncano.library.api.Response;
import com.syncano.library.callbacks.SyncanoCallback;
import com.syncano.library.data.Notification;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity implements SyncServerListener {

    public static final String API_KEY = "<YOUR API KEY>";
    public static final String INSTANCE_NAME = "<YOUR INSTANCE>";
    public static final String CHANNEL = "<YOUR CHANNEL>";

    private ListView consoleListView;
    private Button sendButton;
    private Button clearButton;

    private Syncano syncano;
    private SyncServer syncServer;

    private List<String> list = new LinkedList();
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        consoleListView = (ListView)findViewById(R.id.listViewConsole);
        sendButton = (Button)findViewById(R.id.buttonSend);
        clearButton = (Button)findViewById(R.id.buttonClear);

        syncano = new Syncano(API_KEY, INSTANCE_NAME);
        syncServer = new SyncServer(syncano, this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        consoleListView.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonObject payload = new JsonObject();
                payload.addProperty("player", "player_one");

                final Notification notification = new Notification(null, payload);

                syncano.publishOnChannel(CHANNEL, notification).sendAsync(new SyncanoCallback<Notification>() {
                    @Override
                    public void success(Response response, Notification result) {
                        logConsole("Send success: (id: " + result.getId() + ")");
                    }

                    @Override
                    public void failure(Response response) {
                        logConsole("Send failure: " + response.toString());
                    }
                });
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        syncServer.start(CHANNEL, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncServer.stop();
    }

    @Override
    public void onMessage(Notification notification) {
        logConsole("onMessage: (id: " + notification.getId() + ") " + notification.getPayload());
    }

    @Override
    public void onError(Response<Notification> response) {
        logConsole("onError: " + response.toString());
    }

    Integer numerator = 0;
    private void logConsole(String string) {
        Log.d("logConsole", string);
        list.add(0, numerator.toString() + "| " + string);
        adapter.notifyDataSetChanged();
        consoleListView.smoothScrollToPosition(0);

        numerator++;
    }
}
