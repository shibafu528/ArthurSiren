package info.shibafu528.arthursiren.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import info.shibafu528.arthursiren.util.Const;
import info.shibafu528.libarthur.Quest;
import info.shibafu528.libarthur.SourceExtractor;
import info.shibafu528.libarthur.sources.FamiWiki;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FetchService extends IntentService {

    public static final String EXTRA_RESPONSE_PATH = "response-path";

    private GoogleApiClient googleApiClient;

    public FetchService() {
        super("FetchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            SourceExtractor extractor = new FamiWiki();
            List<Quest> quests = extractor.extractSchedules();

            Collections.sort(quests, new Comparator<Quest>() {
                @Override
                public int compare(Quest lhs, Quest rhs) {
                    return lhs.getStartDate().compareTo(rhs.getStartDate());
                }
            });

            //TODO: Wear側を作るために適当に保存するようにしただけ。SQLite使おう。
            ObjectOutputStream oos = new ObjectOutputStream(openFileOutput(Const.QUESTS_STORED_FILE, MODE_PRIVATE));
            oos.writeObject(quests);
            oos.close();

            if (intent.hasExtra(EXTRA_RESPONSE_PATH)) {
                googleApiClient.blockingConnect(100, TimeUnit.MILLISECONDS);
                if (googleApiClient.isConnected()) {
                    String path = intent.getStringExtra(EXTRA_RESPONSE_PATH);
                    Date now = new Date(System.currentTimeMillis());
                    for (Iterator<Quest> iterator = quests.iterator(); iterator.hasNext(); ) {
                        Quest quest = iterator.next();
                        if (quest.getEndDate().before(now)) {
                            iterator.remove();
                        }
                    }
                    //再シリアライズ
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream responseOos = new ObjectOutputStream(baos);
                    responseOos.writeObject(quests);
                    //データ送信
                    for (Node node : Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes()) {
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, baos.toByteArray()).await();
                    }
                }
                googleApiClient.disconnect();
            } else {
                sendBroadcast(new Intent(Const.ACTION_FETCH_QUESTS));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
