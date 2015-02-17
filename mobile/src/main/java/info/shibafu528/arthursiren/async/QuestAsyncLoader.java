package info.shibafu528.arthursiren.async;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import info.shibafu528.libarthur.Quest;
import info.shibafu528.libarthur.SourceExtractor;
import info.shibafu528.libarthur.sources.FamiWiki;

import java.io.IOException;
import java.util.*;

/**
 * Created by shibafu on 15/01/17.
 */
public class QuestAsyncLoader extends AsyncTaskLoader<List<Quest>> {
    private List<Quest> quests;

    public QuestAsyncLoader(Context context) {
        super(context);
    }

    @Override
    public List<Quest> loadInBackground() {
        if (quests == null) {
            quests = new ArrayList<>();
        } else {
            quests.clear();
        }

        try {
            SourceExtractor extractor = new FamiWiki();
            quests = extractor.extractSchedules();

            Collections.sort(quests, new Comparator<Quest>() {
                @Override
                public int compare(Quest lhs, Quest rhs) {
                    return lhs.getStartDate().compareTo(rhs.getStartDate());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return quests;
    }

    @Override
    protected void onStartLoading() {
        if (quests != null) {
            deliverResult(quests);
        }
        if (takeContentChanged() || quests == null) {
            forceLoad();
        }
    }
}
