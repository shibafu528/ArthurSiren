package info.shibafu528.arthursiren.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import info.shibafu528.arthursiren.service.NotificationService;

/**
 * Created by shibafu on 15/02/03.
 */
public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationService.standby(context);
    }
}
