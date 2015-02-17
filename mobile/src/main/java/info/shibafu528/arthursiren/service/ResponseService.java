package info.shibafu528.arthursiren.service;

import android.content.Intent;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.*;
import info.shibafu528.arthursiren.util.Const;
import info.shibafu528.arthursiren.util.StubUtil;
import info.shibafu528.libarthur.Quest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by shibafu on 15/01/18.
 */
public class ResponseService extends WearableListenerService {

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleApiClient.disconnect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if ("/get-quests".equals(messageEvent.getPath())) {
            if (getFileStreamPath(Const.QUESTS_STORED_FILE).exists()) {
                try {
                    //読み込みとソート
                    List<Quest> quests = StubUtil.loadQuestsStore(getApplicationContext());
                    //再シリアライズ
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(quests);
                    //データ送信
                    for (Node node : Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes()) {
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/get-quests/success", baos.toByteArray()).await();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    //ファイルロードエラーとかの時、失敗した旨を伝える
                    for (Node node : Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes()) {
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/get-quests/failed", null).await();
                    }
                }
            } else {
                Intent service = new Intent(getApplicationContext(), FetchService.class);
                service.putExtra(FetchService.EXTRA_RESPONSE_PATH, "/get-quests/success");
                startService(service);
            }
        }
    }
}
