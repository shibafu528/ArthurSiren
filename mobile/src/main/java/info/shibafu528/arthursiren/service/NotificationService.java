package info.shibafu528.arthursiren.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import info.shibafu528.arthursiren.R;
import info.shibafu528.arthursiren.activity.MainActivity;
import info.shibafu528.arthursiren.util.StubUtil;
import info.shibafu528.libarthur.Quest;
import info.shibafu528.libarthur.QuestTerm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by shibafu on 15/02/03.
 */
public class NotificationService extends IntentService{

    private static final int NOTIFICATION_ID = 0;
    private static final String NOTIFICATION_GROUP = "quest_group";
    private static final long SIX_MINUTES = 360000;

    public static void standby(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Calendar trigger = Calendar.getInstance();
        trigger.set(Calendar.MINUTE, 25);
        trigger.set(Calendar.SECOND, 0);
        if (trigger.getTimeInMillis() < System.currentTimeMillis()) {
            trigger.set(Calendar.MINUTE, 55);
            if (trigger.getTimeInMillis() < System.currentTimeMillis()) {
                trigger.add(Calendar.HOUR_OF_DAY, 1);
                trigger.set(Calendar.MINUTE, 25);
            }
        }
        PendingIntent pend = PendingIntent.getService(context, 0,
                new Intent(context, NotificationService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(), pend);
    }

    public NotificationService() {
        super(NotificationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<Quest> notificationQuests = new ArrayList<>();
        List<Quest> quests;
        try {
            quests = StubUtil.loadQuestsStore(getApplicationContext());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            standby(getApplicationContext());
            return;
        }
        Date now = new Date(System.currentTimeMillis());
        Date afterSix = new Date(System.currentTimeMillis() + SIX_MINUTES);
        for (Quest quest : quests) {
            if (quest.getStartDate().after(now) && quest.getStartDate().before(afterSix)) {
                notificationQuests.add(quest);
            }
        }
        if (!notificationQuests.isEmpty()) {
            NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
            PendingIntent contentIntent = PendingIntent.getActivity(
                    getApplicationContext(), 0,
                    new Intent(getApplicationContext(), MainActivity.class),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationCompat.Action action = new NotificationCompat.Action(
                    R.drawable.ic_action_launch,
                    getString(R.string.launch_game),
                    getMillionArthurPendingIntent(getApplicationContext()));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    .setWhen(notificationQuests.get(0).getStartDate().getTime())
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_stat_quest)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle(getString(R.string.notify_quest_summary))
                    .setContentText(notificationQuests.size() == 1 ? notificationQuests.get(0).getName() :
                            getString(R.string.notify_quest_text, notificationQuests.size()))
                    .setContentIntent(contentIntent)
                    .addAction(action)
                    .setGroup(NOTIFICATION_GROUP)
                    .setGroupSummary(true);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                    .setBigContentTitle(getString(R.string.notify_quest_summary));
            for (int i = 0; i < notificationQuests.size(); i++) {
                Quest notificationQuest = notificationQuests.get(i);
                if (notificationQuest.getTerm() == QuestTerm.STICKY) {
                    continue;
                }

                inboxStyle.addLine(notificationQuest.getName());

                nm.notify(NOTIFICATION_ID + i + 1, new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_stat_quest)
                        .setContentTitle(getString(R.string.notify_quest_card))
                        .setContentText(notificationQuest.getName())
                        .setContentIntent(contentIntent)
                        .addAction(action)
                        .setGroup(NOTIFICATION_GROUP)
                        .build());
            }
            builder.setStyle(inboxStyle);
            nm.notify(NOTIFICATION_ID, builder.build());
        }
        //次回通知を引っ掛ける
        standby(getApplicationContext());
    }

    private static PendingIntent getMillionArthurPendingIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setComponent(new ComponentName("com.square_enix.kairisei_MA", "com.prime31.UnityPlayerNativeActivity"));
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
