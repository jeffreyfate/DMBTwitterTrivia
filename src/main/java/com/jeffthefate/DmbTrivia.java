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
    private static int showStartHour = 20;
    private static boolean getShowStart = false;

    private GameUtil gameUtil = GameUtil.instance();
    private DmbAlmanacUtil dmbAlmanacUtil = DmbAlmanacUtil.instance();
    private GeocodingUtil geocodingUtil = GeocodingUtil.instance();
    private TimeZoneUtil timeZoneUtil = TimeZoneUtil.instance();

    private static Logger logger = Logger.getLogger(DmbTrivia.class);

	public static void main(String args[]) {
		DmbTrivia dmbTrivia = new DmbTrivia(false, false,
                "/home/jeff/dmb-trivia/parseCreds.ser", "/home/jeff/dmb-trivia/dmb.log");
        String triviaDate = new SimpleDateFormat("yyyy-MM-dd-HH")
                .format(new Date());
		dmbTrivia.startListening(null, false, false,
                "/home/jeff/dmb-trivia/lastTriviaScores" + triviaDate + ".ser",
                createSetlistScoresFile());
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
            String credsFile, String logFile) {
        // creates pattern layout
        PatternLayout layout = new PatternLayout();
        String conversionPattern = "[%p] %d %c %M - %m%n";
        layout.setConversionPattern(conversionPattern);

        // creates daily rolling file appender
        DailyRollingFileAppender rollingAppender =
                new DailyRollingFileAppender();
        rollingAppender.setFile(logFile);
        rollingAppender.setDatePattern("'.'yyyy-MM-dd");
        rollingAppender.setLayout(layout);
        rollingAppender.activateOptions();

        // configures the root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.ALL);
        rootLogger.addAppender(rollingAppender);

        // creates a custom logger and log messages
        logger = Logger.getLogger(DmbTrivia.class);
        logger.info("Setting up DMB apps...");
        // Setup to start
        GameUtil gameUtil = GameUtil.instance();
        int questionCount = 34;
        int bonusCount = 6;
        int lightningCount = 6;
        ArrayList<ArrayList<String>> answerList = gameUtil.setupAnswerList(
                isParseDev, credsFile);
        ArrayList<String> replaceList = gameUtil.createReplaceList(isParseDev,
                credsFile);
        ArrayList<String> tipList = gameUtil.createTipList(isParseDev,
                credsFile, "TriviaTip");
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
        final String SETLIST_JPG_FILENAME = "/home/jeff/dmb-trivia/setlist.jpg";
        final String FONT_FILENAME = "/home/jeff/dmb-trivia/roboto.ttf";
        final String BAN_FILE = "/home/jeff/dmb-trivia/banlist.ser";
        final String SCREENSHOT_FILENAME = "/home/jeff/dmb-trivia/TEMP/scores";
        final String PRE_TEXT = "[DMB Trivia] ";
        final String LEADERS_TITLE = "Top Scores";

        final String GAME_TITLE = "Top Setlist Scores";
        final int TRIVIA_MAIN_FONT_SIZE = 34;
        final int TRIVIA_DATE_FONT_SIZE = TRIVIA_MAIN_FONT_SIZE / 2;
        final int LEADERS_LIMIT = 10;
        final int SCORES_TOP_OFFSET = 160;
        final int SCORES_BOTTOM_OFFSET = 80;
        final String TRIVIA_SCORES_FILE = "/home/jeff/dmb-trivia/triviaScores.ser";
        trivia = new Trivia(SETLIST_JPG_FILENAME, FONT_FILENAME,
                LEADERS_TITLE, TRIVIA_MAIN_FONT_SIZE, TRIVIA_DATE_FONT_SIZE,
                LEADERS_LIMIT, SCORES_TOP_OFFSET, SCORES_BOTTOM_OFFSET,
                gameTweetConfig, questionCount, bonusCount, answerList,
                replaceList, tipList, isTwitterDev, PRE_TEXT, lightningCount,
                SCREENSHOT_FILENAME, parse, TRIVIA_SCORES_FILE);
        final int SETLIST_FONT_SIZE = 25;
        final int SETLIST_TOP_OFFSET = 120;
        final int SETLIST_BOTTOM_OFFSET = 20;
        final String SETLIST_DIR = "/home/jeff/dmb-trivia/SETLISTS/";
        final String SETLIST_FILENAME = SETLIST_DIR + "setlist";
        final String LAST_SONG_DIR = "/home/jeff/dmb-trivia/LAST_SONGS/";
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
                credentialUtil.getCredentialedTwitter(parse, false), gameTweetConfig,
                credentialUtil.getCredentialedFacebook(parse), SETLIST_JPG_FILENAME, FONT_FILENAME,
                SETLIST_FONT_SIZE, SETLIST_TOP_OFFSET, SETLIST_BOTTOM_OFFSET,
                GAME_TITLE, TRIVIA_MAIN_FONT_SIZE, TRIVIA_DATE_FONT_SIZE,
                SCORES_TOP_OFFSET, SCORES_BOTTOM_OFFSET, LEADERS_LIMIT,
                SETLIST_FILENAME, LAST_SONG_FILENAME, SETLIST_DIR, BAN_FILE,
                createSetlistScoresFile(), songList, symbolList, GAME_ACCOUNT,
                parse, "setlist", "scores");
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

    /**
     * Get the show start time, given the current date
     *
     * @param now current date, or the given date to check
     * @return
     */
    public long getShowStart(Date now) {
        logger.info("getShowStart: " + now.toString());
        // Fetch the location from dmbalmanac
        String nowString = dmbAlmanacUtil.convertDateToAlmanacFormat(now);
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(now.getTime());
        String showCity = dmbAlmanacUtil.getShowCity(nowString,
                Integer.toString(calendar.get(Calendar.YEAR)));
        if (showCity.contains("ESP")) {
            showCity = showCity.replace("ESP", "ES");
        }
        if (showCity.contains("ITA")) {
            showCity = showCity.replace("ITA", "IT");
        }
        if (showCity.contains("BEL")) {
            showCity = showCity.replace("BEL", "Belgium");
        }
        if (showCity.contains("IRL")) {
            showCity = showCity.replace("IRL", "Ireland");
        }
        // Get the exact location coordinates
        LatLon latLon = geocodingUtil.getCityLatLon(showCity);
        long offset;
        // Find the time zone offset
        try {
            offset = timeZoneUtil.getTimeOffset(latLon);
        } catch (InvalidParameterException e) {
            logger.error("Couldn't determine time zone offset!", e);
            return -1;
        }
        // Set the time to when show start is
        Calendar showTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        showTime.setTimeInMillis(now.getTime());
        logger.info("getShowStart showStartHour: " + showStartHour);
        showTime.set(Calendar.HOUR_OF_DAY, showStartHour);
        showTime.set(Calendar.MINUTE, 0);
        showTime.set(Calendar.SECOND, 0);
        showTime.set(Calendar.MILLISECOND, 0);
        TwitterUtil.instance().sendDirectMessage(gameTweetConfig, "jeffthefate",
                "Show today scheduled for " + new SimpleDateFormat("HHmm z")
                        .format(new Date(showTime.getTimeInMillis() -
                                (offset * 1000))));
        // Change it for the show time zone
        return showTime.getTimeInMillis() - (offset * 1000);
    }

    /**
     * <p>
     *     Check to see if there is a show in the next 24 hours and setup the
     *     start time.
     * </p>
     * <p>
     *     If either it just started listening (server restart) or it is on the
     *     hour, check dmbalmanac.com to see if there is a show in the next 24
     *     hours. If so, fetch the show start time.
     * </p>
     * <p>
     *     Checking for the setlist and listening for setlist game tweets
     *     starts if it is 30 minutes before show start time. Otherwise,
     *     checking starts on the hour, every hour for the Show Begins. Show
     *     length defaults to 6 hours and starts at 2000 local time. So,
     *     watching for the setlist starts at 1930 local time, by default.
     * </p>
     * @param showStart starting point for checking; -2 indicates it is checking
     *                  for the first time; -1 indicates no show is scheduled;
     *                  any other number represents the next show start time, in
     *                  milliseconds since epoch
     * @return the current start time state; one of the above states
     */
    public long showCheck(long showStart, Date now) {
        if (now == null) {
            now = new Date();
        }
        // When there isn't a setlist going on now
        // AND
        // There isn't one already found for today
        // AND
        // It is on the hour
        // OR
        // The server has just be restarted
        // THEN
        // Check for a show happening in the next 24 hours and get start time
        if ((!setlistStarted && showStart < 0 &&
                now.getTime() % 3600000 < 15000) || showStart == -2) {
            logger.info("On the hour, so checking for show");
            // Check with dmbalmanac
            logger.info("showStartHour: " + showStartHour);
            logger.info("now: " + now.toString());
            boolean showIn24Hours = dmbAlmanacUtil.isThereAShowIn24Hours(
                    showStartHour, now);
            logger.info("showIn24Hours: " + showIn24Hours);
            // Check for the show start time if there is one
            if (showIn24Hours) {
                showStart = getShowStart(now);
            }
            // Send update via twitter and indicate no more checking is
            // required
            else {
                showStart = -1;
                // Make sure we don't check again during this time frame
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    logger.error("Sleep interrupted!", e);
                }
            }
        }

        logger.info("getShowStart: " + getShowStart);
        // Force getting the show start time
        if (getShowStart) {
            showStart = getShowStart(now);
            getShowStart = false;
        }
        logger.info("showStart: " + showStart);
        // There is a show found for today
        if (showStart >= 0) {
            logger.info("now: " + now.getTime());
            // TODO Make the 30 minutes dynamic
            // TODO Make the set length dynamic
            // We're right at 30 minutes before show time
            if (now.getTime() >= showStart - 1800000) {
                logger.info("Starting setlist for 6 hours");
                setlist.setDuration(6);
                setlistStarted = true;
                showStart = -1;
            }
            // It is on the hour, so checking for show message
            else if (now.getTime() % 3600000 < 15000) {
                logger.info("Starting setlist for 0 hours");
                setlist.setDuration(0);
                setlistStarted = true;
            }
        }
        return showStart;
    }

    public void startListening(ArrayList<String> files, boolean startSetlist,
            boolean startTrivia, String lastTriviaScoresFile, String lastSetlistScoresFile) {
        final int PRE_SHOW_MINUTES = 15;
        final int PRE_SHOW_TIME = (PRE_SHOW_MINUTES * 60 * 1000);
        final String PRE_SHOW_PRE_TEXT = "[#DMB Trivia] ";
        final String PRE_SHOW_TEXT = "Game starts on @dmbtrivia2 in " +
                PRE_SHOW_MINUTES + " minutes";
        setlistStarted = startSetlist;
        triviaStarted = startTrivia;
        twitterStream.user();
        long showStart = -2;
        while (!kill) {
            showStart = showCheck(showStart, null);
            if (triggerUsername != null && triggerResponse != null) {
                TwitterUtil twitterUtil = TwitterUtil.instance();
                twitterUtil.sendDirectMessage(gameTweetConfig, triggerUsername,
                        triggerResponse);
                triggerUsername = null;
                triggerResponse = null;
            }
            if (triviaStarted) {
                HashMap<Object, Object> lastScores = gameUtil.readScores(
                        lastTriviaScoresFile);
                if (lastScores == null) {
                    lastScores = new HashMap<>();
                }
                trivia.setScoresFile(lastTriviaScoresFile, lastScores);
                trivia.startTrivia(doWarning,
                        PRE_SHOW_PRE_TEXT + PRE_SHOW_TEXT, PRE_SHOW_TIME);
                triviaStarted = false;
            }
            else if (setlistStarted) {
                setlist.setScoresFile(createSetlistScoresFile());
                setlist.startSetlist(files);
                setlistStarted = false;
            }
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                logger.error("Sleep interrupted!", e);
            }
        }
        twitterStream.cleanUp();
        twitterStream.shutdown();
    }

    /**
     * <p>
     * Handles receiving new tweet's in the Twitter stream. When either a new status or direct message
     * is received, processes it and takes action on it accordingly.
     * </p>
     * <p>
     * Direct messages that trigger actions:
     * <ul>
     * <li>start trivia [skip] {QUESTION COUNT} {LIGHTNING COUNT} {BONUS COUNT}</li>
     * <li>start setlist {HOURS} [test]</li>
     * <li>end setlist</li>
     * <li>kill</li>
     * <li>ban {TWITTER HANDLE}</li>
     * <li>unban {TWITTER HANDLE}</li>
     * <li>current scores [image]</li>
     * <li>final scores [image]</li>
     * <li>start time {LOCAL HOUR}</li>
     * </ul>
     * </p>
     *
     * Status messages are passed down to the {@link com.jeffthefate.setlist.Setlist} and
     * {@link com.jeffthefate.Trivia} objects for processing.
     */
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
                        // TODO make dynamic
						// Default to 5 hours for now
						setlist.setDuration(5);
						if (countList.size() == 1) {
							setlist.setDuration(countList.get(0));
						}
					}
                    if (dmText.contains("test")) {
                        setlist.setDuration(0);
                        setlist.setUrl("/home/jeff/dmb-trivia/test2014-06-20.txt");
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
                    } else {
                        setlist.postSetlistScoresText(false);
                    }
                } else if (massagedText.contains("start time")) {
                    // Grab the start time hour
                    if (dmText.matches(".*\\d.*")) {
                        String temp;
                        ArrayList<Integer> countList = new ArrayList<Integer>(0);
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
                        if (countList.size() == 1) {
                            if (countList.get(0) < 12) {
                                showStartHour = countList.get(0) + 12;
                            }
                            else {
                                showStartHour = countList.get(0);
                            }
                        }
                    }
                    else {
                        showStartHour = 20;
                    }
                    logger.info("showStartHour: " + showStartHour);
                    // Toggle boolean to recalculate start time
                    getShowStart = true;
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

		public void onStatus(Status status) {
            if (logger != null) {
                logger.info("onStatus:");
                logger.info(status.getUser().getScreenName());
                logger.info(status.getText());
            }
			trivia.processTweet(status);
			setlist.processTweet(status);
		}

        public void onDeletionNotice(StatusDeletionNotice arg0) {}
        public void onScrubGeo(long arg0, long arg1) {}
        public void onStallWarning(StallWarning arg0) {}
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

    public static String createSetlistScoresFile() {
        String setlistDate = new SimpleDateFormat("yyyy-MM-dd")
                .format(new Date());
        return "/home/jeff/dmb-trivia/lastSetlistScores" + setlistDate + ".ser";
    }

}
