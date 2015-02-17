package info.shibafu528.libarthur.sources;

import info.shibafu528.libarthur.Quest;
import info.shibafu528.libarthur.QuestTerm;
import info.shibafu528.libarthur.SourceExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shibafu on 15/01/16.
 */
public class FamiWiki implements SourceExtractor {
    private static final Pattern DATE_SPAN_PATTERN = Pattern.compile("(\\d{1,2})月(\\d{1,2})日?\\([月火水木金土日]\\)(?:[〜～~](\\d{1,2})月(\\d{1,2})日?\\([月火水木金土日]\\))?");
    private static final Pattern DATE_SPLIT_PATTERN = Pattern.compile("(\\d{1,2}月)?(\\d{1,2})日?\\([月火水木金土日]\\)");
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{1,2})");
    private Document document;

    public FamiWiki() throws IOException {
        this.document = Jsoup.connect("http://wiki.famitsu.com/kairi/").get();
    }

    public FamiWiki(Document document) {
        this.document = document;
    }

    @Override
    public List<Quest> extractSchedules() {
        List<Quest> quests = new ArrayList<>();
        Element table = document.getElementById("xpd_content1001").getElementsByTag("table").first();

        String lastQuestName = "";
        List<Date> lastQuestDates = new ArrayList<>();

        for (Element tr : table.getElementsByTag("tr")) {
            if ("ゲリラ情報".equals(tr.child(0).text())) {
                continue;
            }
            int virtualNodeSize = tr.childNodeSize() + ("常駐".equals(tr.children().last().text())? 1 : 0);
            switch (virtualNodeSize) {
                case 4:
                    //名前, 日付, 時刻, 時刻
                    lastQuestName = tr.child(0).text();
                case 3:
                    lastQuestDates = parseDateString(tr.child(virtualNodeSize % 3).text());
                case 2:
                    if ("常駐".equals(tr.children().last().text())) {
                        Date firstDate = lastQuestDates.get(0);
                        Date lastDate = lastQuestDates.get(lastQuestDates.size()-1);
                        quests.add(new Quest(lastQuestName, QuestTerm.STICKY, firstDate, new Date(lastDate.getTime() + 86399999L)));
                    } else {
                        long startTime = getTimeOfDay(tr.child(tr.childNodeSize() - 2));
                        long endTime = getTimeOfDay(tr.child(tr.childNodeSize() - 1));
                        for (Date date : lastQuestDates) {
                            quests.add(new Quest(lastQuestName, QuestTerm.GUERRILLA,
                                    new Date(date.getTime() + startTime),
                                    new Date(date.getTime() + endTime)));
                        }
                    }
                    break;
                case 1:
                    //日付(, 常駐)
                    lastQuestDates = parseDateString(tr.child(0).text());
                    Date firstDate = lastQuestDates.get(0);
                    Date lastDate = lastQuestDates.get(lastQuestDates.size()-1);
                    quests.add(new Quest(lastQuestName, QuestTerm.STICKY, firstDate, new Date(lastDate.getTime() + 86399999L)));
                    break;
            }
        }
        return quests;
    }

    private List<Date> parseDateString(String dateString) {
        dateString = dateString.replace(" ", "");
        List<Date> dates = new ArrayList<>();
        if (dateString.contains(",") || dateString.contains("、")) {
            //カンマ区切りのパースを行う
            int month = 0;
            for (String s : dateString.split("[,、]")) {
                Matcher matcher = DATE_SPLIT_PATTERN.matcher(s);
                if (matcher.find()) {
                    if (matcher.group(1) != null && !"".equals(matcher.group(1))) {
                        month = Integer.parseInt(matcher.group(1).replace("月", "")) - 1;
                    }
                    int day = Integer.parseInt(matcher.group(matcher.groupCount()));

                    dates.add(getCalendar(month, day).getTime());
                }
            }
        } else {
            //期間のパースを行う
            Matcher matcher = DATE_SPAN_PATTERN.matcher(dateString);
            if (matcher.find()) {
                int startMonth = Integer.parseInt(matcher.group(1)) - 1;
                int startDay = Integer.parseInt(matcher.group(2));
                int endMonth = 0, endDay = 0;
                if (matcher.group(3) != null) {
                    endMonth = Integer.parseInt(matcher.group(3)) - 1;
                    endDay = Integer.parseInt(matcher.group(4));
                } else if (matcher.find()) {
                    endMonth = Integer.parseInt(matcher.group(1)) - 1;
                    endDay = Integer.parseInt(matcher.group(2));
                }
                if (endDay > 0) {
                    Calendar start = getCalendar(startMonth, startDay);
                    Calendar end = getCalendar(endMonth, endDay);
                    for (; start.getTimeInMillis() <= end.getTimeInMillis(); start = (Calendar) start.clone(), start.add(Calendar.DAY_OF_MONTH, 1)) {
                        dates.add(start.getTime());
                    }
                } else {
                    dates.add(getCalendar(startMonth, startDay).getTime());
                }
            }
        }
        return dates;
    }

    private Calendar getCalendar(int month, int day) {
        Calendar calendar = Calendar.getInstance(Locale.JAPAN);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private long getTimeOfDay(Element element) {
        Matcher matcher = TIME_PATTERN.matcher(element.text());
        if (matcher.find() && matcher.groupCount() == 2) {
            long hour = Long.parseLong(matcher.group(1)) * 60 * 60 * 1000;
            long minute = Long.parseLong(matcher.group(2)) * 60 * 1000;
            return hour + minute;
        }
        return 0;
    }
}
