package com.jeffthefate;

import com.jeffthefate.setlist.Setlist;
import com.jeffthefate.utils.CredentialUtil;
import com.jeffthefate.utils.FileUtil;
import com.jeffthefate.utils.GameUtil;
import com.jeffthefate.utils.Parse;
import com.jeffthefate.utils.json.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import twitter4j.*;
import twitter4j.conf.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 * 
 */
public class DmbTrivia {

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
    private static final String SCREENSHOT_FILENAME = "/home/TEMP/scores";
	
	private static final String PRE_SHOW_PRE_TEXT = "[#DMB Trivia] ";
	private static final String PRE_TEXT = "[DMB Trivia] ";
	private static final String LEADERS_TITLE = "Top Scores";
	private static final String PRE_SHOW_TEXT = "Game starts on @dmbtrivia2 in 15 minutes";

	private static final int SETLIST_FONT_SIZE = 60;
    private static final int SETLIST_TOP_OFFSET = 180;
    private static final int SETLIST_BOTTOM_OFFSET = 60;
	
	private static final int TRIVIA_MAIN_FONT_SIZE = 60;
	private static final int TRIVIA_DATE_FONT_SIZE = TRIVIA_MAIN_FONT_SIZE - 30;
	private static final int LEADERS_LIMIT = 10;
	private static final int SCORES_TOP_OFFSET = 200;
    private static final int SCORES_BOTTOM_OFFSET = 40;
	private static final int PRE_SHOW_TIME = (15 * 60 * 1000);
	
	private static Setlist setlist;
    
    private static ArrayList<ArrayList<String>> songList;
	private static ArrayList<String> symbolList;

	private static ArrayList<ArrayList<String>> answerList;
	private static HashMap<String, String> acronymMap;
	private static ArrayList<String> replaceList;
	private static ArrayList<String> tipList;

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

    private static Configuration gameTweetConfig;
    // TODO Store this somewhere secure
    private Parse parse;
    private FileUtil fileUtil = FileUtil.instance();
    private JsonUtil jsonUtil = JsonUtil.instance();
    private CredentialUtil credentialUtil = CredentialUtil.instance();

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

		DmbTrivia dmbTrivia = new DmbTrivia(false, false);
		dmbTrivia.startListening(null, false);
	}

    public static Setlist getSetlist() {
        return setlist;
    }

    public static void setSetlist(Setlist setlist) {
        DmbTrivia.setlist = setlist;
    }

    public DmbTrivia(boolean isTwitterDev, boolean isParseDev) {
        // Setup to start
        GameUtil gameUtil = GameUtil.instance();
        answerList = gameUtil.setupAnswerList();
        acronymMap = gameUtil.createAcronymMap();
        replaceList = gameUtil.createReplaceList();
        tipList = gameUtil.createTipList();
        songList = gameUtil.generateSongMatchList();
        symbolList = gameUtil.generateSymbolList();
        parse = credentialUtil.getCredentialedParse(isParseDev);
        gameTweetConfig = isTwitterDev ? credentialUtil.getCredentialedTwitter(
                parse, false) : credentialUtil.getCredentialedTwitter(parse,
                true);
        if (logger != null) {
            logger.info("Setup params:");
            logger.info("questions: " + questionCount);
            logger.info("lightning: " + lightningCount);
            logger.info("bonus: " + bonusCount);
            logger.info("twitter dev: " + isTwitterDev);
            logger.info("parse dev: " + isParseDev);
        }
        trivia = new Trivia(SETLIST_JPG_FILENAME, ROBOTO_FONT_FILENAME,
                LEADERS_TITLE, TRIVIA_MAIN_FONT_SIZE, TRIVIA_DATE_FONT_SIZE,
                LEADERS_LIMIT, SCORES_TOP_OFFSET, SCORES_BOTTOM_OFFSET,
                gameTweetConfig, questionCount, bonusCount, answerList,
                acronymMap, replaceList, tipList, isTwitterDev, PRE_TEXT,
                lightningCount, SCREENSHOT_FILENAME, parse);
        setlist = new Setlist(null, isTwitterDev,
                credentialUtil.getCredentialedTwitter(parse, false),
                gameTweetConfig, SETLIST_JPG_FILENAME, ROBOTO_FONT_FILENAME,
                SETLIST_FONT_SIZE, SETLIST_TOP_OFFSET, SETLIST_BOTTOM_OFFSET,
                SETLIST_FILENAME, LAST_SONG_FILENAME, SETLIST_DIR, BAN_FILE,
                songList, symbolList, "dmbtrivia2", parse, "setlist", "scores");
        twitterStream = new TwitterStreamFactory(gameTweetConfig).getInstance();
        twitterStream.addRateLimitStatusListener(new RateLimitStatusListener() {
            public void onRateLimitReached(RateLimitStatusEvent event) {
                if (logger != null) {
                    logger.error("Rate limit reached!");
                    logger.error("Limit: " + event.getRateLimitStatus()
                            .getLimit());
                    logger.error("Remaining: "
                            + event.getRateLimitStatus().getRemaining());
                    logger.error("Reset time: "
                            + event.getRateLimitStatus()
                            .getResetTimeInSeconds());
                    logger.error("Seconds until reset: "
                            + event.getRateLimitStatus()
                            .getSecondsUntilReset());
                }
            }

            public void onRateLimitStatus(RateLimitStatusEvent event) {
                if (logger != null) {
                    logger.warn("Rate limit event!");
                    logger.warn("Limit: " + event.getRateLimitStatus()
                            .getLimit());
                    logger.warn("Remaining: "
                            + event.getRateLimitStatus().getRemaining());
                }
            }
        });
        twitterStream.addListener(streamListener);
    }

    public void startListening(ArrayList<String> files, boolean startSetlist) {
        setlistStarted = startSetlist;
        twitterStream.user();
        while (true) {
            if (triviaStarted) {
                trivia.startTrivia(doWarning,
                        PRE_SHOW_PRE_TEXT + PRE_SHOW_TEXT, PRE_SHOW_TIME);
                triviaStarted = false;
            }
            else if (setlistStarted) {
                setlist.startSetlist(files);
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
        }
    }

	private static void sendDirectMessage(Configuration tweetConfig,
			String screenName, String message) {
		Twitter twitter = new TwitterFactory(tweetConfig).getInstance();
		try {
			twitter.sendDirectMessage(screenName, message);
		} catch (TwitterException e) {
            if (logger != null) {
                logger.error("Unable to send direct message!");
            }
			e.printStackTrace();
		}
	}

	static UserStreamListener streamListener = new UserStreamListener() {
		public void onDirectMessage(DirectMessage dm) {
			String dmText = dm.getText();
            if (logger != null) {
                logger.debug("Direct message text: " + dmText);
                logger.debug("Sender: " + dm.getSenderScreenName());
            }
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
                    if (logger != null) {
                        logger.info("Warning: " + doWarning);
                    }
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
                    if (dmText.contains("test")) {
                        setlist.setDuration(0);
                        setlist.setUrl("/home/test2014-06-20.txt");
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
						setlist.postSetlistScoresImage(true);
					}
					else {
						setlist.postSetlistScoresText(true);
					}
				} else if (massagedText.contains("current scores")) {
					if (massagedText.contains("image")) {
						setlist.postSetlistScoresImage(false);
					}
					else {
						setlist.postSetlistScoresText(false);
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
            if (logger != null) {
                logger.info("onStatus:");
                logger.info(status.getUser().getScreenName());
                logger.info(status.getText());
            }
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
