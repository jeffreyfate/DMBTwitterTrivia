package com.jeffthefate;

import com.jeffthefate.utils.FileUtil;
import junit.framework.TestCase;

import java.io.File;

public class DmbTriviaTest extends TestCase {

    private final String USER_DIR = System.getProperty("user.dir") +
            File.separator + "target" + File.separator;
    private final String SETLIST_DIR = USER_DIR + "SETLISTS" + File.separator;
    private final String SETLIST_FILENAME = SETLIST_DIR + "setlist";
    private final String LAST_SONG_DIR = USER_DIR + "LAST_SONG" +
            File.separator;
    private final String LAST_SONG_FILENAME = LAST_SONG_DIR + "last_song";
    private final String TEST_RESOURCES = "src/test/resources/";
    private final String TEST_RESOURCES_SET = "src/test/resources/set/";
    private final String TEST_JPG = TEST_RESOURCES + "setlist.jpg";
    private final String TEST_FONT = TEST_RESOURCES + "roboto.ttf";
    private final String TEST_BAN = TEST_RESOURCES + "ban.ser";

    private FileUtil fileUtil = FileUtil.instance();

    public void testDmbTriviaFullSet() {
        /*
        System.out.println(USER_DIR);
    	// Use dev/test Parse and Twitter credentials
        DmbTrivia dmbTrivia = new DmbTrivia(true, true);
        // Setup app as usual for the most part
        Setlist setlist = dmbTrivia.getSetlist();
        setlist.setSetlistFilename(SETLIST_FILENAME);
        setlist.setLastSongFilename(LAST_SONG_FILENAME);
        setlist.setSetlistDir(SETLIST_DIR);
        setlist.setSetlistJpgFilename(TEST_JPG);
        setlist.setFontFilename(TEST_FONT);
        setlist.setBanFile(TEST_BAN);
        // Grab the list of test files
        ArrayList<String> files = fileUtil.getListOfFiles(TEST_RESOURCES_SET,
                ".txt");
        // Cycle through each, setting the URL to the file after a wait
        dmbTrivia.startListening(files, true);
        */
    }
}
