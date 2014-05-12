package com.jeffthefate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import twitter4j.DirectMessage;
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
	private static final String PROD_KEY = "eMuFTZYxt3X35zhiOmnOyJuAS";
	private static final String PROD_SECRET = "0ITfF0A1Ew6wNvJUFKpxVgF6qCdKk8nLPgluSFhsfDnvURp6Xu";
	private static final String PROD_ACCESS_TOKEN = "2357105641-VsaREbnEYoiyi0pb3s68Ucgsr6E9iGHjQzqtctS";
	private static final String PROD_ACCESS_SECRET = "MmsrjVpjneQnf3s8FyXfzWK98h6o8dqu5M4xsZEd4S6kv";
	private static final String PROD_NAME = "dmbtrivia2";

	private static final String DEV_KEY = "BXx60ptC4JAMBQLQ965H3g";
	private static final String DEV_SECRET = "0ivTqB1HKqQ6t7HQhIl0tTUNk8uRnv1nhDqyFXBw";
	private static final String DEV_ACCESS_TOKEN = "1265342035-6mYSoxlw8NuZSdWX0AS6cpIu3We2CbCev6rbKUQ";
	private static final String DEV_ACCESS_SECRET = "XqxxE4qLUK3wJ4LHlIbcSP1m6G4spZVmCDdu5RLuU";
	private static final String DEV_NAME = "dmbtriviatest";

	private static String CURR_KEY = PROD_KEY;
	private static String CURR_SECRET = PROD_SECRET;
	private static String CURR_ACCESS_TOKEN = PROD_ACCESS_TOKEN;
	private static String CURR_ACCESS_SECRET = PROD_ACCESS_SECRET;
	private static String CURR_NAME = PROD_NAME;

	private static final String SETLIST_JPG_FILENAME = "/home/setlist.jpg";
	private static final String ROBOTO_FONT_FILENAME = "/home/roboto.ttf";
	private static final String PRE_SHOW_PRE_TEXT = "[#DMB Trivia] ";
	private static final String PRE_TEXT = "[DMB Trivia] ";
	private static final String LEADERS_TITLE = "Top Scores";
	private static final String PRE_SHOW_TEXT = "Game starts in 15 minutes";

	private static final int TRIVIA_MAIN_FONT_SIZE = 30;
	private static final int TRIVIA_DATE_FONT_SIZE = TRIVIA_MAIN_FONT_SIZE - 10;
	private static final int LEADERS_LIMIT = 10;
	private static final int PRE_SHOW_TIME = (15 * 60 * 1000);

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

	private static boolean triviaStarted = false;

	static {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		System.setProperty("currentDate", dateFormat.format(new Date()));
	}

	private static Logger logger = Logger.getLogger(DmbTrivia.class);

	public static void main(String args[]) {
		logger.info("Setting up trivia...");
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
				CURR_NAME = DEV_NAME;
				isDev = true;
			} else {
				CURR_KEY = PROD_KEY;
				CURR_SECRET = PROD_SECRET;
				CURR_ACCESS_TOKEN = PROD_ACCESS_TOKEN;
				CURR_ACCESS_SECRET = PROD_ACCESS_SECRET;
				CURR_NAME = PROD_NAME;
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
		Configuration tweetConfig = setupTweet();
		logger.info("Setup params:");
		logger.info("questions: " + questionCount);
		logger.info("lightning: " + lightningCount);
		logger.info("bonus: " + bonusCount);
		logger.info("dev: " + isDev);
		trivia = new Trivia(SETLIST_JPG_FILENAME, ROBOTO_FONT_FILENAME,
				LEADERS_TITLE, TRIVIA_MAIN_FONT_SIZE, TRIVIA_DATE_FONT_SIZE,
				LEADERS_LIMIT, tweetConfig, questionCount, bonusCount, nameMap,
				acronymMap, replaceList, tipList, isDev, PRE_TEXT,
				lightningCount);
		twitterStream = new TwitterStreamFactory(tweetConfig).getInstance();
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
		while (true) {
			if (triviaStarted) {
				trivia.startTrivia(doWarning,
						PRE_SHOW_PRE_TEXT + PRE_SHOW_TEXT, PRE_SHOW_TIME);
				triviaStarted = false;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			if (triggerUsername != null && triggerResponse != null) {
				sendDirectMessage(tweetConfig, triggerUsername, triggerResponse);
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

	private static Configuration setupTweet() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(CURR_KEY)
				.setOAuthConsumerSecret(CURR_SECRET)
				.setOAuthAccessToken(CURR_ACCESS_TOKEN)
				.setOAuthAccessTokenSecret(CURR_ACCESS_SECRET);
		return cb.build();
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
		// tipList.add("1st place gets full points, 2nd place 75% full points, 3rd place 50% full points");
		// tipList.add("There are three rounds: first uses regular point values, second adds 500 points extra, bonus round adds 1000 extra");
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
				if (dmText.toLowerCase(Locale.getDefault()).contains(
						"start trivia")) {
					if (dmText.toLowerCase(Locale.getDefault())
							.contains("skip")) {
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
				} else {
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
