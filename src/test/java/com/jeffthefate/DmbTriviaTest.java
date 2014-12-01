package com.jeffthefate;

import com.jeffthefate.setlist.Setlist;
import com.jeffthefate.utils.FileUtil;
import junit.framework.TestCase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DmbTriviaTest extends TestCase {

    private final String USER_DIR = System.getProperty("user.dir") +
            File.separator + "target" + File.separator;
    private final String SETLIST_DIR = USER_DIR + "SETLISTS" + File.separator;
    private final String SETLIST_FILENAME = SETLIST_DIR + "setlist";
    private final String LAST_SONG_DIR = USER_DIR + "LAST_SONG" +
            File.separator;
    private final String LAST_SONG_FILENAME = LAST_SONG_DIR + "last_song";

    private FileUtil fileUtil = FileUtil.instance();

    private DmbTrivia dmbTrivia;
    private Setlist setlist;

    private final String TEST_RESOURCES_SET = "src/test/resources/set/";
    private final String TEST_RESOURCES = "src/test/resources/";
    private final String TEST_JPG = TEST_RESOURCES + "setlist.jpg";
    private final String TEST_FONT = TEST_RESOURCES + "roboto.ttf";
    private final String TEST_BAN = TEST_RESOURCES + "ban.ser";

    public void setUp() throws Exception {
        super.setUp();
        // Use dev/test Parse and Twitter credentials
        dmbTrivia = new DmbTrivia(true, true, "parseCreds", "dmb.log");
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        final String TEST_SCORES = TEST_RESOURCES + "scores" + date + ".ser";
        // Setup app as usual for the most part
        setlist = dmbTrivia.getSetlist();
        setlist.setSetlistFilename(SETLIST_FILENAME);
        setlist.setLastSongFilename(LAST_SONG_FILENAME);
        setlist.setSetlistDir(SETLIST_DIR);
        File setlistFile = new File(SETLIST_DIR);
        File lastSongFile = new File(LAST_SONG_DIR);
        setlistFile.mkdir();
        lastSongFile.mkdir();
        setlist.setSetlistJpgFilename(TEST_JPG);
        setlist.setFontFilename(TEST_FONT);
        setlist.setBanFile(TEST_BAN);
        setlist.setScoresFile(TEST_SCORES);
    }

    public void testDmbTriviaFullSet() {
        // Grab the list of test files
        ArrayList<String> files = fileUtil.getListOfFiles(TEST_RESOURCES_SET,
                ".txt");
        // Cycle through each, setting the URL to the file after a wait
        String date = new SimpleDateFormat("yyyy-MM-dd-HH")
                .format(new Date());
        /*
        dmbTrivia.startListening(files, true, false, "lastTriviaScores" + date +
                ".ser", "lastSetlistScores" + date + ".ser");
        */
    }

    public void testDmbTriviaGame() {
        String date = new SimpleDateFormat("yyyy-MM-dd-HH")
                .format(new Date());
        /*
        dmbTrivia.startListening(null, false, true, "lastTriviaScores" + date +
                ".ser");
        */
    }

    public void testGetShowStart() {
        /*
        Calendar cal = new GregorianCalendar(2014, 7, 31, 0, 0, 0);
        assertEquals("Show start incorrect!", 1409536800000l,
                dmbTrivia.getShowStart(cal.getTime(), 2014));
        cal.set(2014, 6, 5);
        assertEquals("Show start incorrect!", 1404604800000l,
                dmbTrivia.getShowStart(cal.getTime(), 2014));
        */
    }

    public void testShowCheck() {
        //assertEquals("Show start doesn't match!", -1, dmbTrivia.showCheck(-2));
        assertEquals("Show start doesn't match!", -1, dmbTrivia.showCheck(-1));
    }

    public void testXLong() {
        setlist.setSetlistFilename("SETLISTS\\setlist");
        setlist.setLastSongFilename("LAST_SONG\\last_song");
        setlist.setSetlistDir("SETLISTS\\");
        File setlistFile = new File("SETLISTS\\");
        File lastSongFile = new File("LAST_SONG\\");
        setlistFile.mkdir();
        lastSongFile.mkdir();
        /*
        dmbTrivia.startListening(null, false, "D:\\triviaScoresTest.ser");
        */
    }
}
