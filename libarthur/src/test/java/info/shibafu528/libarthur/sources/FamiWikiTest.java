package info.shibafu528.libarthur.sources;

import info.shibafu528.libarthur.Quest;
import org.jsoup.Jsoup;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

public class FamiWikiTest {

    @Test
    public void testExtractSchedules() throws Exception {
        InputStream is = getClass().getResourceAsStream("samplesource.html");
        try {
            FamiWiki extractor = new FamiWiki(Jsoup.parse(is, "utf-8", "http://wiki.famitsu.com/kairi/"));
            List<Quest> quests = extractor.extractSchedules();
            for (Quest quest : quests) {
                System.out.println(quest);
            }
        } finally {
            is.close();
        }
    }

    @Test
    public void testExtractSchedulesFromWeb() throws Exception {
        FamiWiki extractor = new FamiWiki();
        List<Quest> quests = extractor.extractSchedules();
        for (Quest quest : quests) {
            System.out.println(quest);
        }
    }
}