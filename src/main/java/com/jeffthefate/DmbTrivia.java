package com.jeffthefate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.jeffthefate.setlist.Setlist;

import twitter4j.DirectMessage;
import twitter4j.FilterQuery;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Hello world!
 * 
 */
public class DmbTrivia {
	/*
	 * private static final String PROD_KEY = "LG23SHRfn5E5aFld0kc9sdLEG";
	 * private static final String PROD_SECRET =
	 * "EBsmiO5Aj9chSVQGElZ5falYWX02Dqw4GmdgEOekkuDHMlHGOX"; private static
	 * final String PROD_ACCESS_TOKEN =
	 * "611044728-ojJ9OszvvtV4ATU36JYZhhdk9BoaDfvbHFzUzqvY"; private static
	 * final String PROD_ACCESS_SECRET =
	 * "jrjcTJWSnzUBAaXpYXMEZazVhEHohoXlDktd9a2kM6rE5"; private static final
	 * String PROD_NAME = "dmbtrivia";
	 */
	private static final String PROD_KEY = "LG23SHRfn5E5aFld0kc9sdLEG";
	private static final String PROD_SECRET = "EBsmiO5Aj9chSVQGElZ5falYWX02Dqw4GmdgEOekkuDHMlHGOX";
	private static final String PROD_ACCESS_TOKEN = "611044728-ojJ9OszvvtV4ATU36JYZhhdk9BoaDfvbHFzUzqvY";
	private static final String PROD_ACCESS_SECRET = "jrjcTJWSnzUBAaXpYXMEZazVhEHohoXlDktd9a2kM6rE5";
	private static final String PROD_ACCOUNT = "dmbtrivia";
	
	private static final String DEV_KEY = "BXx60ptC4JAMBQLQ965H3g";
	private static final String DEV_SECRET = "0ivTqB1HKqQ6t7HQhIl0tTUNk8uRnv1nhDqyFXBw";
	private static final String DEV_ACCESS_TOKEN = "1265342035-6mYSoxlw8NuZSdWX0AS6cpIu3We2CbCev6rbKUQ";
	private static final String DEV_ACCESS_SECRET = "XqxxE4qLUK3wJ4LHlIbcSP1m6G4spZVmCDdu5RLuU";
	private static final String DEV_ACCOUNT = "dmbtriviatest";
	
	private static final String TRIVIA2_KEY = "eMuFTZYxt3X35zhiOmnOyJuAS";
	private static final String TRIVIA2_SECRET = "0ITfF0A1Ew6wNvJUFKpxVgF6qCdKk8nLPgluSFhsfDnvURp6Xu";
	private static final String TRIVIA2_ACCESS_TOKEN = "2357105641-VsaREbnEYoiyi0pb3s68Ucgsr6E9iGHjQzqtctS";
	private static final String TRIVIA2_ACCESS_SECRET = "MmsrjVpjneQnf3s8FyXfzWK98h6o8dqu5M4xsZEd4S6kv";
	private static final String TRIVIA2_ACCOUNT = "dmbtrivia2";

	private static String CURR_KEY = PROD_KEY;
	private static String CURR_SECRET = PROD_SECRET;
	private static String CURR_ACCESS_TOKEN = PROD_ACCESS_TOKEN;
	private static String CURR_ACCESS_SECRET = PROD_ACCESS_SECRET;
	private static String CURR_ACCOUNT = PROD_ACCOUNT;

	private static final String SETLIST_DIR = "/home/SETLISTS/";
    private static final String SETLIST_FILENAME = SETLIST_DIR + "setlist";
    private static final String SETLIST_FILENAME_DEV = SETLIST_DIR +
    		"setlist_dev";
	private static final String LAST_SONG_DIR = "/home/LAST_SONGS/";
    private static final String LAST_SONG_FILENAME = LAST_SONG_DIR +
    		"last_song";
    private static final String LAST_SONG_FILENAME_DEV = LAST_SONG_DIR +
    		"last_song_dev";
	private static final String SETLIST_JPG_FILENAME = "/home/setlist.jpg";
	private static final String ROBOTO_FONT_FILENAME = "/home/roboto.ttf";
	private static final String BAN_FILE = "/home/banlist.ser";
	
	private static final String PRE_SHOW_PRE_TEXT = "[#DMB Trivia] ";
	private static final String PRE_TEXT = "[DMB Trivia] ";
	private static final String LEADERS_TITLE = "Top Scores";
	private static final String PRE_SHOW_TEXT = "Game starts on @dmbtrivia2 in 15 minutes";

	private static final int SETLIST_FONT_SIZE = 60;
    private static final int SETLIST_VERTICAL_OFFSET = 180;
	
	private static final int TRIVIA_MAIN_FONT_SIZE = 60;
	private static final int TRIVIA_DATE_FONT_SIZE = TRIVIA_MAIN_FONT_SIZE - 30;
	private static final int LEADERS_LIMIT = 10;
	private static final int LEADERS_VERTICAL_OFFSET = 200;
	private static final int PRE_SHOW_TIME = (15 * 60 * 1000);
	
	private static Setlist setlist;
    
    private static ArrayList<ArrayList<String>> songList = new ArrayList<ArrayList<String>>(0);
	private static ArrayList<String> symbolList = new ArrayList<String>(0);

	private static ArrayList<ArrayList<String>> nameMap = new ArrayList<ArrayList<String>>(
			0);
	private static HashMap<String, String> acronymMap = new HashMap<String, String>();
	private static ArrayList<String> replaceList = new ArrayList<String>(0);
	private static ArrayList<String> tipList = new ArrayList<String>(0);

	private static Trivia trivia;
	private static int questionCount = 34;
	private static int bonusCount = 6;
	private static int lightningCount = 6;
	private static boolean doWarning = true;

	private static TwitterStream twitterStream;
	private static String triggerUsername = null;
	private static String triggerResponse = null;

	private static boolean setlistStarted = false;
	private static boolean triviaStarted = false;

	private static Logger logger;

	public static void main(String args[]) {
		// creates pattern layout
        PatternLayout layout = new PatternLayout();
        String conversionPattern = "[%p] %d %c %M - %m%n";
        layout.setConversionPattern(conversionPattern);
 
        // creates daily rolling file appender
        DailyRollingFileAppender rollingAppender =
        		new DailyRollingFileAppender();
        rollingAppender.setFile("/home/dmb.log");
        rollingAppender.setDatePattern("'.'yyyy-MM-dd");
        rollingAppender.setLayout(layout);
        rollingAppender.activateOptions();
 
        // configures the root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        rootLogger.addAppender(rollingAppender);
 
        // creates a custom logger and log messages
        logger = Logger.getLogger(DmbTrivia.class);
		logger.info("Setting up DMB apps...");
		try {
			if (args.length > 0) {
				questionCount = Integer.valueOf(args[0]);
			}
			if (args.length > 1) {
				lightningCount = Integer.valueOf(args[1]);
			}
			if (args.length > 2) {
				bonusCount = Integer.valueOf(args[2]);
			}
		} catch (NumberFormatException e) {
			logger.warn("Bad argument!");
			return;
		}
		if (args.length > 3) {
			doWarning = Boolean.valueOf(args[3]);
		}
		boolean isDev = false;
		if (args.length > 4) {
			if (Boolean.parseBoolean(args[4])) {
				CURR_KEY = DEV_KEY;
				CURR_SECRET = DEV_SECRET;
				CURR_ACCESS_TOKEN = DEV_ACCESS_TOKEN;
				CURR_ACCESS_SECRET = DEV_ACCESS_SECRET;
				CURR_ACCOUNT = DEV_ACCOUNT;
				isDev = true;
			} else {
				CURR_KEY = PROD_KEY;
				CURR_SECRET = PROD_SECRET;
				CURR_ACCESS_TOKEN = PROD_ACCESS_TOKEN;
				CURR_ACCESS_SECRET = PROD_ACCESS_SECRET;
				CURR_ACCOUNT = PROD_ACCOUNT;
				isDev = false;
			}
		}
		/*
		 * Twitter twitter = new TwitterFactory(setupTweet()).getInstance(); try
		 * { Map<String, RateLimitStatus> statusMap =
		 * twitter.getRateLimitStatus(); for (Entry<String, RateLimitStatus>
		 * status : statusMap.entrySet()) { System.out.println(status.getKey() +
		 * " : " + status.getValue().getLimit() + " : " +
		 * status.getValue().getRemaining() + " : " +
		 * status.getValue().getResetTimeInSeconds() + " : " +
		 * status.getValue().getSecondsUntilReset()); } } catch
		 * (TwitterException e) { e.printStackTrace(); }
		 */
		// Setup to start
		setupAnswerMap();
		setupSongList();
		Configuration setlistTweetConfig = setupTweet(false);
		Configuration gameTweetConfig = setupTweet(true);
		logger.info("Setup params:");
		logger.info("questions: " + questionCount);
		logger.info("lightning: " + lightningCount);
		logger.info("bonus: " + bonusCount);
		logger.info("dev: " + isDev);
		trivia = new Trivia(SETLIST_JPG_FILENAME, ROBOTO_FONT_FILENAME,
				LEADERS_TITLE, TRIVIA_MAIN_FONT_SIZE, TRIVIA_DATE_FONT_SIZE,
				LEADERS_LIMIT, LEADERS_VERTICAL_OFFSET, gameTweetConfig,
				questionCount, bonusCount, nameMap,
				acronymMap, replaceList, tipList, isDev, PRE_TEXT,
				lightningCount);
		// TODO Add url and isDev parameters to DM
		setlist = new Setlist(null, isDev, setlistTweetConfig, gameTweetConfig,
				SETLIST_JPG_FILENAME, ROBOTO_FONT_FILENAME,
				SETLIST_FONT_SIZE, SETLIST_VERTICAL_OFFSET,
				isDev ? SETLIST_FILENAME_DEV : SETLIST_FILENAME,
				isDev ? LAST_SONG_FILENAME_DEV : LAST_SONG_FILENAME,
				SETLIST_DIR, BAN_FILE, songList, symbolList, TRIVIA2_ACCOUNT);
		twitterStream = new TwitterStreamFactory(gameTweetConfig).getInstance();
		twitterStream.addRateLimitStatusListener(new RateLimitStatusListener() {
			public void onRateLimitReached(RateLimitStatusEvent event) {
				logger.error("Rate limit reached!");
				logger.error("Limit: " + event.getRateLimitStatus().getLimit());
				logger.error("Remaining: "
						+ event.getRateLimitStatus().getRemaining());
				logger.error("Reset time: "
						+ event.getRateLimitStatus().getResetTimeInSeconds());
				logger.error("Seconds until reset: "
						+ event.getRateLimitStatus().getSecondsUntilReset());
			}

			public void onRateLimitStatus(RateLimitStatusEvent event) {
				logger.warn("Rate limit event!");
				logger.warn("Limit: " + event.getRateLimitStatus().getLimit());
				logger.warn("Remaining: "
						+ event.getRateLimitStatus().getRemaining());
			}
		});
		twitterStream.addListener(streamListener);
		twitterStream.user();
		//twitterStream.filter(new FilterQuery(0, new long[] {},
		//		new String[] {"dmbtrivia2"}));
		while (true) {
			if (triviaStarted) {
				trivia.startTrivia(doWarning,
						PRE_SHOW_PRE_TEXT + PRE_SHOW_TEXT, PRE_SHOW_TIME);
				triviaStarted = false;
			}
			else if (setlistStarted) {
				setlist.startSetlist();
				setlistStarted = false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			if (triggerUsername != null && triggerResponse != null) {
				sendDirectMessage(gameTweetConfig, triggerUsername,
						triggerResponse);
				triggerUsername = null;
				triggerResponse = null;
			}
			/*
			 * logger.info("startTrivia: " + startTrivia); if (startTrivia) {
			 * twitterStream.removeListener(streamListener);
			 * twitterStream.shutdown();
			 * twitterStream.addListener(streamListener); twitterStream.user();
			 * logger.info("setting startTrivia to false"); startTrivia = false;
			 * }
			 */
		}
	}

	private static void sendDirectMessage(Configuration tweetConfig,
			String screenName, String message) {
		Twitter twitter = new TwitterFactory(tweetConfig).getInstance();
		try {
			twitter.sendDirectMessage(screenName, message);
		} catch (TwitterException e) {
			logger.error("Unable to send direct message!");
			e.printStackTrace();
		}
	}
	
	private static Configuration setupTweet(boolean isGame) {
    	ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(isGame ? TRIVIA2_KEY : CURR_KEY)
		  .setOAuthConsumerSecret(isGame ? TRIVIA2_SECRET : CURR_SECRET)
		  .setOAuthAccessToken(isGame ? TRIVIA2_ACCESS_TOKEN : CURR_ACCESS_TOKEN)
		  .setOAuthAccessTokenSecret(isGame ? TRIVIA2_ACCESS_SECRET : CURR_ACCESS_SECRET);
		return cb.build();
    }
	
	private static void setupSongList() {
		ArrayList<String> tempList = new ArrayList<String>(0);
    	tempList.add("belly belly nice");
    	tempList.add("bbn");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("the riff");
    	tempList.add("riff");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("true");
    	tempList.add("true reflections");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("dido");
    	tempList.add("drive in drive out");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("lyd");
    	tempList.add("let you down");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("liog");
    	tempList.add("lie in our graves");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("proudest");
    	tempList.add("pm");
    	tempList.add("proudest monkey");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("smts");
    	tempList.add("so much to say");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("billies");
    	tempList.add("tripping billies");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("byah");
    	tempList.add("build you a house");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ggt");
    	tempList.add("good good time");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ioy");
    	tempList.add("idea of you");
    	tempList.add("the idea of you");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ktk");
    	tempList.add("kill the king");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("lw");
    	tempList.add("loving wings");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("suad");
    	tempList.add("sweet up and down");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("astb");
    	tempList.add("anyone seen the bridge");
    	tempList.add("anyone seen the bridge?");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("wwbom");
    	tempList.add("what will become of me");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ftwii");
    	tempList.add("funny");
    	tempList.add("funny the way it is");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("lithog");
    	tempList.add("lying in the hands of god");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("smlam");
    	tempList.add("shake me like a monkey");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("wia");
    	tempList.add("why i am");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("you & me");
    	tempList.add("you and me");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("bef");
    	tempList.add("big eyed fish");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("dad");
    	tempList.add("digging a ditch");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("gig");
    	tempList.add("grace is gone");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("kkj");
    	tempList.add("kit kat jam");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("wayg");
    	tempList.add("where are you going");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ynk");
    	tempList.add("you never know");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ants");
    	tempList.add("ants marching");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("bowa");
    	tempList.add("best of what's around");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("jimi");
    	tempList.add("jimi thing");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("lld");
    	tempList.add("lover lay down");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("pfwyg");
    	tempList.add("pay for what you get");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("rhyme");
    	tempList.add("rhyme & reason");
    	tempList.add("rhyme and reason");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("typical");
    	tempList.add("typical situation");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("wwys");
    	tempList.add("what would you say");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("dftr");
    	tempList.add("reaper");
    	tempList.add("the reaper");
    	tempList.add("don't fear the reaper");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("afm");
    	tempList.add("angel from montgomery");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("bdth");
    	tempList.add("burning down the house");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("cortez");
    	tempList.add("cortez the killer");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("dbtr");
    	tempList.add("down by the river");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("fitr");
    	tempList.add("fool in the rain");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ftbow");
    	tempList.add("for the beauty of wynona");
    	tempList.add("beauty of wynona");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("fhtsa");
    	tempList.add("funny how time slips away");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("gtbt");
    	tempList.add("good times bad times");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("hhmm");
    	tempList.add("hey hey my my");
    	tempList.add("hey hey my my into the black");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("maker");
    	tempList.add("the maker");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("lbv");
    	tempList.add("long black veil");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("majdbts");
    	tempList.add("me and julio down by the schoolyard");
    	tempList.add("me and julio");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("needle and the damage down");
    	tempList.add("the needle and the damage done");
    	tempList.add("tnatdd");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("siu");
    	tempList.add("stir it up");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("thank you");
    	tempList.add("thank you falettinme be mice elf agin");
    	tempList.add("tyfbmea");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("tots");
    	tempList.add("time of the season");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("abi");
    	tempList.add("american baby intro");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ewu");
    	tempList.add("everybody wake up");
    	tempList.add("everybody wake up (our finest hour arrives)");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("bayou");
    	tempList.add("louisiana bayou");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("odh");
    	tempList.add("old dirt hill");
    	tempList.add("old dirt hill (bring that beat back)");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("oomh");
    	tempList.add("out of my hands");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("stand up");
    	tempList.add("stand up (for it)");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("steady");
    	tempList.add("steady as we go");
    	tempList.add("sawg");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("stolen");
    	tempList.add("stolen away");
    	tempList.add("stolen away on 55th & 3rd");
    	tempList.add("stolen away on 55th and 3rd");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ymdt");
    	tempList.add("die trying");
    	tempList.add("you might die trying");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("dreams of our fathers");
    	tempList.add("doof");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ftt");
    	tempList.add("fool to think");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("idi");
    	tempList.add("i did it");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("iihia");
    	tempList.add("if i had it all");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("stdh");
    	tempList.add("sleep to dream her");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("tsb");
    	tempList.add("the space between");
    	tempList.add("space between");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("wya");
    	tempList.add("what you are");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("wtwe");
    	tempList.add("when the world ends");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ibyu");
    	tempList.add("i'll back you up");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("osw");
    	tempList.add("one sweet world");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("stjl");
    	tempList.add("tstjl");
    	tempList.add("song that jane likes");
    	tempList.add("the song that jane likes");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("loml");
    	tempList.add("love of my life");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("gbe");
    	tempList.add("grey blue eyes");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("sdl");
    	tempList.add("so damn lucky");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("stay or leave");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("too high");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("uaa");
    	tempList.add("up and away");
    	tempList.add("up & away");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("ddtw");
    	tempList.add("don't drink the water");
    	tempList.add("don't drink");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("dreaming tree");
    	tempList.add("the dreaming tree");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("the last stop");
    	tempList.add("last stop");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("pnp");
    	tempList.add("pantala naga pampa");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("stay");
    	tempList.add("stay (wasting time)");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("stone");
    	tempList.add("the stone");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("crash");
    	tempList.add("crash into me");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("hunger");
    	tempList.add("hunger for the great light");
    	tempList.add("hftgl");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("nancies");
    	tempList.add("dancing nancies");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("corn bread");
    	tempList.add("cornbread");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("help myself");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("joyride");
    	tempList.add("joy ride");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("lrb");
    	tempList.add("little red bird");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("write a song");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("watchtower");
    	tempList.add("all along the watchtower");
    	tempList.add("aatw");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("sugarman");
    	tempList.add("sugar man");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("dreamgirl");
    	tempList.add("dream girl");
    	songList.add(tempList);
    	tempList = new ArrayList<String>(0);
    	tempList.add("#36");
    	tempList.add("#36 jam");
    	tempList.add("36");
    	tempList.add("36 jam");
    	songList.add(tempList);
    	symbolList.add("*");
		symbolList.add("+");
    	symbolList.add("~");
    	symbolList.add("^");
    	symbolList.add("§");
    	symbolList.add("¤");
    	symbolList.add("$");
    	symbolList.add("%");
	}

	private static void setupAnswerMap() {
		ArrayList<String> tempList = new ArrayList<String>(0);
		tempList.add("dave");
		tempList.add("dave matthews");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("boyd");
		tempList.add("boyd tinsley");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("stefan");
		tempList.add("stefan lessard");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("carter");
		tempList.add("carter beauford");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("leroi");
		tempList.add("leroi moore");
		tempList.add("roi");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("butch");
		tempList.add("butch taylor");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("tim");
		tempList.add("tim reynolds");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("jeff");
		tempList.add("jeff coffin");
		tempList.add("coffin");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("rashawn");
		tempList.add("rashawn ross");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("lillywhite");
		tempList.add("steve lillywhite");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("lawlor");
		tempList.add("joe lawlor");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("fenton");
		tempList.add("fenton williams");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("peter");
		tempList.add("peter griesar");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("sax");
		tempList.add("saxophone");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("alpine");
		tempList.add("alpine valley");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("big whiskey and the groogrux king");
		tempList.add("big whiskey");
		tempList.add("big whiskey & the groogrux king");
		tempList.add("bwggk");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("stay");
		tempList.add("stay (wasting time)");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("you and me");
		tempList.add("you & me");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("rhyme and reason");
		tempList.add("rhyme & reason");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("funny the way it is");
		tempList.add("ftwii");
		tempList.add("funny");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("sweet up & down");
		tempList.add("sweet up and down");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("billies");
		tempList.add("tripping billies");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("bela fleck and the flecktones");
		tempList.add("bela fleck & the flecktones");
		tempList.add("the flecktones");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
		tempList.add("any noise");
		tempList.add("any noise/anti-noise");
		tempList.add("any noise anti-noise");
		tempList.add("any noise anti noise");
		tempList.add("any noise antinoise");
		tempList.add("anynoise antinoise");
		nameMap.add(tempList);
		tempList = new ArrayList<String>(0);
    	tempList.add("dreamgirl");
    	tempList.add("dream girl");
    	nameMap.add(tempList);
		acronymMap.put("btcs", "before these crowded streets");
		acronymMap.put("uttad", "under the table and dreaming");
		acronymMap.put("watchtower", "all along the watchtower");
		acronymMap.put("hunger", "hunger for the great light");
		acronymMap.put("crash", "crash into me");
		acronymMap.put("nancies", "dancing nancies");
		acronymMap.put("msg", "madison square garden");
		acronymMap.put("wpb", "west palm beach");
		acronymMap.put("ddtw", "dont drink the water");
		replaceList.add("the ");
		replaceList.add("his ");
		replaceList.add("her ");
		tipList.add("Scoring: #1 - Full points, #2 - 3/4 points, #4 - 1/2 points\nIf you protect tweets we must follow you to play (ask us)");
		tipList.add("You won't see people guess who protect their tweets unless you follow each other");
		tipList.add("Only one guess per person is accepted for each question");
		tipList.add("Note: We have a free DMB Trivia & Setlist app in the Google Play Store https://play.google.com/store/apps/details?id=com.jeffthefate.dmbquiz");
		/*
		 * nameMap.put("dave", "dave matthews"); nameMap.put("boyd",
		 * "boyd tinsley"); nameMap.put("stefan", "stefan lessard");
		 * nameMap.put("carter", "carter beauford"); nameMap.put("leroi moore",
		 * "leroi"); nameMap.put("roi", "leroi moore"); nameMap.put("leroi",
		 * "roi"); nameMap.put("butch", "butch taylor"); nameMap.put("tim",
		 * "tim reynolds"); nameMap.put("jeff", "jeff coffin");
		 * nameMap.put("jeff coffin", "coffin"); nameMap.put("rashawn",
		 * "rashawn ross"); nameMap.put("lillywhite", "steve lillywhite");
		 * acronymMap.put("btcs", "before these crowded streets");
		 * acronymMap.put("uttad", "under the table and dreaming");
		 * nameMap.put("sax", "saxophone"); acronymMap.put("watchtower",
		 * "all along the watchtower"); acronymMap.put("hunger",
		 * "hunger for the great light"); acronymMap.put("crash",
		 * "crash into me"); acronymMap.put("nancies", "dancing nancies");
		 * acronymMap.put("big whiskey", "big whiskey and the groogrux king");
		 * acronymMap.put("msg", "madison square garden"); nameMap.put("alpine",
		 * "alpine valley"); acronymMap.put("wpb", "west palm beach");
		 */
	}

	static UserStreamListener streamListener = new UserStreamListener() {
		public void onDirectMessage(DirectMessage dm) {
			String dmText = dm.getText();
			logger.debug("Direct message text: " + dmText);
			logger.debug("Sender: " + dm.getSenderScreenName());
			if ((dm.getSenderScreenName().equalsIgnoreCase("copperpot5") || dm
					.getSenderScreenName().equalsIgnoreCase("jeffthefate"))) {
				triggerUsername = dm.getSenderScreenName();
				String massagedText = dm.getText().toLowerCase(
						Locale.getDefault()); 
				if (massagedText.contains("start trivia")) {
					if (massagedText.contains("skip")) {
						doWarning = false;
					} else {
						doWarning = true;
					}
					ArrayList<Integer> countList = new ArrayList<Integer>(0);
					String temp;
					if (dmText.matches(".*\\d.*")) {
						Pattern p = Pattern.compile("\\d+");
						Matcher m = p.matcher(dmText);
						while (m.find()) {
							temp = m.group();
							try {
								countList.add(Integer.parseInt(temp));
							} catch (NumberFormatException e) {
								e.printStackTrace();
								return;
							}
						}
						trivia.setQuestionCount(34);
						trivia.setLightningCount(6);
						trivia.setBonusCount(6);
						if (countList.size() == 3) {
							trivia.setQuestionCount(countList.get(0));
							trivia.setLightningCount(countList.get(1));
							trivia.setBonusCount(countList.get(2));
						}
					}
					logger.info("Warning: " + doWarning);
					triggerResponse = "Command received! Starting trivia "
							+ "game: " + doWarning + ", "
							+ trivia.getQuestionCount() + ", "
							+ trivia.getLightningCount() + ", "
							+ trivia.getBonusCount();
					triviaStarted = true;
				} else if (massagedText.contains("start setlist")) {
					ArrayList<Integer> countList = new ArrayList<Integer>(0);
					String temp;
					if (dmText.matches(".*\\d.*")) {
						Pattern p = Pattern.compile("\\d+");
						Matcher m = p.matcher(dmText);
						while (m.find()) {
							temp = m.group();
							try {
								countList.add(Integer.parseInt(temp));
							} catch (NumberFormatException e) {
								e.printStackTrace();
								return;
							}
						}
						// Default to 5 hours for now
						setlist.setDuration(5);
						if (countList.size() == 1) {
							setlist.setDuration(countList.get(0));
						}
					}
					triggerResponse = "Command received! Starting setlist: "
							+ setlist.getDurationHours() + " hours";
					setlistStarted = true;
				} else if (massagedText.contains("end setlist")) {
					setlist.setKill(true);
				} else if (massagedText.contains("unban")) {
					setlist.unbanUser(StringUtils.strip(massagedText.replace(
							"unban", "")));
				} else if (massagedText.contains("ban")) {
					setlist.banUser(StringUtils.strip(massagedText.replace(
							"ban", "")));
				} else if (massagedText.contains("final scores")) {
					if (massagedText.contains("image")) {
						setlist.postSetlistScoresImage(setlist.FINAL_SCORES);
					}
					else {
						setlist.postSetlistScoresText(setlist.FINAL_SCORES);
					}
				} else if (massagedText.contains("current scores")) {
					if (massagedText.contains("image")) {
						setlist.postSetlistScoresImage(setlist.CURRENT_SCORES);
					}
					else {
						setlist.postSetlistScoresText(setlist.CURRENT_SCORES);
					}
				} else if (!massagedText.contains("end setlist") &&
						!massagedText.contains("final scores") &&
						!massagedText.contains("current scores")) {
					triggerResponse = "Unrecognized command! Valid command: "
							+ "start trivia [skip] {QUESTIONS} {LIGHTNING} "
							+ "{BONUS}";
				}
			} else {
				triggerUsername = null;
				triggerResponse = null;
			}
		}

		public void onDeletionNotice(StatusDeletionNotice arg0) {
		}

		public void onScrubGeo(long arg0, long arg1) {
		}

		public void onStallWarning(StallWarning arg0) {
		}

		public void onStatus(Status status) {
			logger.info("onStatus:");
			logger.info(status.getUser().getScreenName());
			logger.info(status.getText());
			trivia.processTweet(status);
			setlist.processTweet(status);
		}

		public void onTrackLimitationNotice(int arg0) {
		}

		public void onException(Exception arg0) {
		}

		public void onBlock(User arg0, User arg1) {
		}

		public void onDeletionNotice(long arg0, long arg1) {
		}

		public void onFavorite(User arg0, User arg1, Status arg2) {
		}

		public void onFollow(User arg0, User arg1) {
		}

		public void onFriendList(long[] arg0) {
		}

		public void onUnblock(User arg0, User arg1) {
		}

		public void onUnfavorite(User arg0, User arg1, Status arg2) {
		}

		public void onUnfollow(User arg0, User arg1) {
		}

		public void onUserListCreation(User arg0, UserList arg1) {
		}

		public void onUserListDeletion(User arg0, UserList arg1) {
		}

		public void onUserListMemberAddition(User arg0, User arg1, UserList arg2) {
		}

		public void onUserListMemberDeletion(User arg0, User arg1, UserList arg2) {
		}

		public void onUserListSubscription(User arg0, User arg1, UserList arg2) {
		}

		public void onUserListUnsubscription(User arg0, User arg1, UserList arg2) {
		}

		public void onUserListUpdate(User arg0, UserList arg1) {
		}

		public void onUserProfileUpdate(User arg0) {
		}
	};

}
