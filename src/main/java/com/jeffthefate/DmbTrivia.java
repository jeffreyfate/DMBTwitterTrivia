package com.jeffthefate;

import com.jeffthefate.setlist.Setlist;
import com.jeffthefate.utils.*;
import com.jeffthefate.utils.json.JsonUtil;
import com.jeffthefate.utils.json.geocoding.LatLon;
import com.jeffthefate.utils.json.parse.Credential;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import twitter4j.*;
import twitter4j.conf.Configuration;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 * 
 */
public class DmbTrivia {

	private TwitterStream twitterStream;
    private Configuration gameTweetConfig;

    private static Setlist setlist;
    private static Trivia trivia;
    private static boolean doWarning = true;
    private static boolean setlistStarted = false;
    private static boolean triviaStarted = false;
    private static String triggerUsername = null;
    private static String triggerResponse = null;
    private static boolean kill = false;

    private FileUtil fileUtil;
    private DmbAlmanacUtil dmbAlmanacUtil = DmbAlmanacUtil.instance();
    private GeocodingUtil geocodingUtil = GeocodingUtil.instance();
    private TimeZoneUtil timeZoneUtil = TimeZoneUtil.instance();

    private static Logger logger = Logger.getLogger(DmbTrivia.class);

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

		DmbTrivia dmbTrivia = new DmbTrivia(false, false,
                "/home/parseCreds.ser");
        String date = new SimpleDateFormat("yyyy-MM-dd-HH")
                .format(new Date());
		dmbTrivia.startListening(null, false, "/home/lastTriviaScores" + date +
                ".ser", "/home/lastSetlistScores" + date + ".ser");
	}

    public Setlist getSetlist() {
        return setlist;
    }

    public void setSetlist(Setlist setlist) {
        DmbTrivia.setlist = setlist;
    }

    public static Trivia getTrivia() { return trivia; }

    public static void setTrivia(Trivia trivia) { DmbTrivia.trivia = trivia; }

    public DmbTrivia(boolean isTwitterDev, boolean isParseDev,
            String credsFile) {
        // Setup to start
        GameUtil gameUtil = GameUtil.instance();
        fileUtil = FileUtil.instance();
        int questionCount = 34;
        int bonusCount = 6;
        int lightningCount = 6;
        ArrayList<ArrayList<String>> answerList = gameUtil.setupAnswerList(
                isParseDev, credsFile);
        ArrayList<String> replaceList = gameUtil.createReplaceList(isParseDev,
                credsFile);
        ArrayList<String> tipList = gameUtil.createTriviaTipList(isParseDev,
                credsFile);
        ArrayList<ArrayList<String>> songList = gameUtil
                .generateSongMatchList(isParseDev, credsFile);
        ArrayList<String> symbolList = gameUtil.generateSymbolList(isParseDev,
                credsFile);
        CredentialUtil credentialUtil = CredentialUtil.instance();
        Parse parse = credentialUtil.getCredentialedParse(isParseDev,
                credsFile);
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
        final String SETLIST_JPG_FILENAME = "/home/setlist.jpg";
        final String FONT_FILENAME = "/home/roboto.ttf";
        final String BAN_FILE = "/home/banlist.ser";
        final String SETLIST_SCORES_FILE = "/home/setlistScores.ser";
        final String SCREENSHOT_FILENAME = "/home/TEMP/scores";
        final String PRE_TEXT = "[DMB Trivia] ";
        final String LEADERS_TITLE = "Top Scores";

        final String GAME_TITLE = "Top Setlist Scores";
        final int TRIVIA_MAIN_FONT_SIZE = 34;
        final int TRIVIA_DATE_FONT_SIZE = TRIVIA_MAIN_FONT_SIZE / 2;
        final int LEADERS_LIMIT = 10;
        final int SCORES_TOP_OFFSET = 160;
        final int SCORES_BOTTOM_OFFSET = 80;
        final String TRIVIA_SCORES_FILE = "/home/triviaScores.ser";
        trivia = new Trivia(SETLIST_JPG_FILENAME, FONT_FILENAME,
                LEADERS_TITLE, TRIVIA_MAIN_FONT_SIZE, TRIVIA_DATE_FONT_SIZE,
                LEADERS_LIMIT, SCORES_TOP_OFFSET, SCORES_BOTTOM_OFFSET,
                gameTweetConfig, questionCount, bonusCount, answerList,
                replaceList, tipList, isTwitterDev, PRE_TEXT, lightningCount,
                SCREENSHOT_FILENAME, parse, TRIVIA_SCORES_FILE);
        final int SETLIST_FONT_SIZE = 25;
        final int SETLIST_TOP_OFFSET = 120;
        final int SETLIST_BOTTOM_OFFSET = 20;
        final String SETLIST_DIR = "/home/SETLISTS/";
        final String SETLIST_FILENAME = SETLIST_DIR + "setlist";
        final String LAST_SONG_DIR = "/home/LAST_SONGS/";
        final String LAST_SONG_FILENAME = LAST_SONG_DIR + "last_song";
        JsonUtil jsonUtil = JsonUtil.instance();
        List<Credential> credentialList = jsonUtil.getCredentialResults(
                parse.get("Credential", "")).getResults();
        String GAME_ACCOUNT = "";
        for (Credential credential : credentialList) {
            switch(credential.getName()) {
                case "gameAccount":
                    GAME_ACCOUNT = credential.getValue();
                    break;
            }
        }
        setlist = new Setlist(null, isTwitterDev,
                credentialUtil.getCredentialedTwitter(parse, false),
                gameTweetConfig, SETLIST_JPG_FILENAME, FONT_FILENAME,
                SETLIST_FONT_SIZE, SETLIST_TOP_OFFSET, SETLIST_BOTTOM_OFFSET,
                GAME_TITLE, TRIVIA_MAIN_FONT_SIZE, TRIVIA_DATE_FONT_SIZE,
                SCORES_TOP_OFFSET, SCORES_BOTTOM_OFFSET, LEADERS_LIMIT,
                SETLIST_FILENAME, LAST_SONG_FILENAME, SETLIST_DIR, BAN_FILE,
                SETLIST_SCORES_FILE, songList, symbolList, GAME_ACCOUNT, parse,
                "setlist", "scores");
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

    public long getShowStart(Date now, int year) {
        String nowString = dmbAlmanacUtil
                .convertDateToAlmanacFormat(now);
        String showCity = dmbAlmanacUtil.getShowCity(nowString,
                Integer.toString(year));
        LatLon latLon = geocodingUtil.getCityLatLon(showCity);
        long offset;
        try {
            offset = timeZoneUtil.getTimeOffset(latLon);
        } catch (InvalidParameterException e) {
            logger.error("Couldn't determine time zone offset!", e);
            return -1;
        }
        Calendar showTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        // TODO What can we do instead of hard coding?
        showTime.setTime(now);
        showTime.set(Calendar.HOUR_OF_DAY, 19);
        showTime.set(Calendar.MINUTE, 0);
        showTime.set(Calendar.SECOND, 0);
        showTime.set(Calendar.MILLISECOND, 0);
        return showTime.getTimeInMillis() - (offset * 1000);
    }

    public long showCheck(long showStart) {
        Calendar midnight = new GregorianCalendar();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        Date now = new Date();
        if (now.after(midnight.getTime())) {
            logger.info("After midnight");
            midnight.set(Calendar.SECOND, 15);
            if (now.before(midnight.getTime()) || showStart == -2) {
                logger.info("Right after midnight");
                boolean showToday = dmbAlmanacUtil.isThereAShowToday(null,
                        null);
                logger.info("showToday: " + showToday);
                if (showToday) {
                    showStart = getShowStart(now, midnight.get(Calendar.YEAR));
                }
                else {
                    showStart = -1;
                }
            }
        }
        logger.info("showStart: " + showStart);
        if (showStart >= 0) {
            logger.info("now: " + now.getTime());
            if (now.getTime() >= showStart - 1800000) {
                logger.info("Starting setlist for 5 hours");
                setlist.setDuration(5);
                setlistStarted = true;
                showStart = -1;
            }
            else if (now.getTime() % 3600000 < 15000) {
                logger.info("Starting setlist for 0 hours");
                setlist.setDuration(0);
                setlistStarted = true;
            }
        }
        return showStart;
    }

    public void startListening(ArrayList<String> files, boolean startSetlist,
            String lastTriviaScoresFile, String lastSetlistScoresFile) {
        final int PRE_SHOW_MINUTES = 15;
        final int PRE_SHOW_TIME = (PRE_SHOW_MINUTES * 60 * 1000);
        final String PRE_SHOW_PRE_TEXT = "[#DMB Trivia] ";
        final String PRE_SHOW_TEXT = "Game starts on @dmbtrivia2 in " +
                PRE_SHOW_MINUTES + " minutes";
        setlistStarted = startSetlist;
        twitterStream.user();
        long showStart = -2;
        while (!kill) {
            showStart = showCheck(showStart);
            if (triviaStarted) {
                String lastScores = fileUtil.readStringFromFile(
                        lastTriviaScoresFile);
                if (lastScores == null) {
                    lastScores = "";
                }
                trivia.setScoresFile(lastTriviaScoresFile, lastScores);
                trivia.startTrivia(doWarning,
                        PRE_SHOW_PRE_TEXT + PRE_SHOW_TEXT, PRE_SHOW_TIME);
                triviaStarted = false;
            }
            else if (setlistStarted) {
                if (setlist.getDurationHours() != 0) {
                    setlist.setScoresFile(lastSetlistScoresFile);
                }
                setlist.startSetlist(files);
                setlistStarted = false;
            }
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                logger.error("Sleep interrupted!", e);
            }
            if (triggerUsername != null && triggerResponse != null) {
                TwitterUtil twitterUtil = TwitterUtil.instance();
                twitterUtil.sendDirectMessage(gameTweetConfig, triggerUsername,
                        triggerResponse);
                triggerUsername = null;
                triggerResponse = null;
            }
        }
        twitterStream.cleanUp();
        twitterStream.shutdown();
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
                    doWarning = !massagedText.contains("skip");
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
                } else if (massagedText.contains("kill")) {
                    kill = true;
                    setlist.setKill(kill);
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

		public void onTrackLimitationNotice(int arg0) {}
		public void onException(Exception arg0) {}
		public void onBlock(User arg0, User arg1) {}
		public void onDeletionNotice(long arg0, long arg1) {}
		public void onFavorite(User arg0, User arg1, Status arg2) {}
		public void onFollow(User arg0, User arg1) {}
		public void onFriendList(long[] arg0) {}
		public void onUnblock(User arg0, User arg1) {}
		public void onUnfavorite(User arg0, User arg1, Status arg2) {}
		public void onUnfollow(User arg0, User arg1) {}
		public void onUserListCreation(User arg0, UserList arg1) {}
		public void onUserListDeletion(User arg0, UserList arg1) {}
		public void onUserListMemberAddition(User arg0, User arg1, UserList arg2) {}
		public void onUserListMemberDeletion(User arg0, User arg1, UserList arg2) {}
		public void onUserListSubscription(User arg0, User arg1, UserList arg2) {}
		public void onUserListUnsubscription(User arg0, User arg1, UserList arg2) {}
		public void onUserListUpdate(User arg0, UserList arg1) {}
        public void onUserProfileUpdate(User arg0) {}
	};

}
