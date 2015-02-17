package info.shibafu528.arthursiren.util;

import android.content.Context;
import info.shibafu528.libarthur.Quest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shibafu on 15/01/18.
 */
public final class StubUtil {
    public static List<Quest> loadQuestsStore(Context context) throws IOException, ClassNotFoundException {
        List<Quest> quests;
        ObjectInputStream ois = new ObjectInputStream(context.openFileInput(Const.QUESTS_STORED_FILE));
        quests = (List<Quest>) ois.readObject();
        if (quests != null) {
            Date now = new Date(System.currentTimeMillis());
            for (Iterator<Quest> iterator = quests.iterator(); iterator.hasNext(); ) {
                Quest quest = iterator.next();
                if (quest.getEndDate().before(now)) {
                    iterator.remove();
                }
            }
        } else {
            quests = new ArrayList<>();
        }
        return quests;
    }
}
