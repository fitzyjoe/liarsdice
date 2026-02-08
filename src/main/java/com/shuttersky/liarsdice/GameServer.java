package com.shuttersky.liarsdice;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * This class is the meat and potatoes of the liar's dice game.  The purpose
 * is to coordinate the game between multiple Players.  The GameServer enforces
 * the rules, coordinates communication between the players, solicits bids,
 * ensures fair-play, and determines the winner of the match.
 */
public class GameServer
{
    /**
     * the name of the game log file
     */
    public static final String GAME_LOG = "game";

    /**
     * extension to use for the game log
     */
    public static final String GAME_LOG_EXT = "log";

    /**
     * the name of the debug log file
     */
    private static final String DEBUG_LOG = "debug.log";

    /**
     * you may override the debug level by defining this property
     */
    private static final String PROPERTY_DEBUG_LEVEL = "debuglevel";

    /**
     * you have to have at least 2 players to play
     */
    private static final int MIN_NUM_PLAYERS = 2;

    /**
     * if not overridden, this is the number of seconds that each player has to make a decision
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 1;

    /**
     * the timeout for each player can be configured by defining this property
     */
    private static final String PROPERTY_TIMEOUT = "timeout";

    /**
     * the number of games to play
     */
    private static final String PROPERTY_NUMGAMES = "numgames";

    /**
     * if no property is set for PROPERTY_NUMGAMES this default value is used
     */
    private static final int DEFAULT_NUMGAMES = 1;

    /**
     * member variables representing the player classes and their cups.
     */
    private ArrayList<Player> players = null;
    private ArrayList<Cup> playerCups = null;
    private RoundState rs = null;
    private GameState gamestate = null;
    private int currentPlayer = 0;
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private int numGames = DEFAULT_NUMGAMES;
    private int currentGameNumber = 0;

    /**
     * static logger object.  This should be used by all objects in the game.
     */
    public static Logger logger = null;

    static
    {
        setupLogger();
    }

    /**
     * Player classes are read from the properties, and the players
     * are pitted against each other.
     *
     * @param args command line parameters are not currently used
     */
    public static void main(String[] args)
    {
        logger.entering("GameServer", "main");

        logger.info("welcome to liar's dice");

        // make a new gameServer glass
        GameServer gs = new GameServer();

        gs.loadOptions();

        while (gs.currentGameNumber < gs.numGames)
        {
            // play the game
            try
            {
                gs.PlayGame();
            }
            catch (Exception e)
            {
                logger.severe("Exception during game play");
                System.exit(-1);
            }

            try
            {
                // save the outcome of the game to the debug log
                gs.gamestate.logResults(gs.getFormattedGameNumber());

                // serialize the gamestate for the game viewer
                gs.saveGameState();
            }
            catch (Exception e)
            {
                logger.severe("Unable to save the game to a file");
                System.exit(-1);
            }

            gs.currentGameNumber++;
        }

        logger.exiting("GameServer", "main");
    }

    /**
     * Plays one game of liars dice.
     */
    private void PlayGame() throws Exception
    {
        // create a gamestate to keep statistics
        gamestate = new GameState();

        // load the options and players from the defined properties
        logger.finest("loading players");
        try
        {
            loadPlayers();
        }
        catch (Exception e)
        {
            logger.severe("Failed to load players");
            throw new Exception("Failed to load players", e);
        }

        // create a cup for each player
        logger.finest("creating cups");
        try
        {
            makeCups();
        }
        catch (Exception e)
        {
            logger.severe("Cannot make cups for players");
            throw new Exception("Cannot make cups for players", e);
        }

        logger.fine("begin play");
        logger.finest("number of players: " + players.size());

        // while there is more than one player
        while (players.size() > 1)
        {
            try
            {
                playRound();
            }
            catch (Exception e)
            {
                logger.severe("Exception thrown while playing a round.  The game is ending early.");
                throw new Exception("Exception thrown while playing a round.  The game is ending early.", e);
            }
        }

        logger.info("The winner is " + (players.get(0)).getClass().getSimpleName());
    }

    /**
     * plays one round of liars dice.  A round consists of
     * <ul>
     * <li>shaking everyone's cups
     * <li>administering the solicitation for bids
     * <li>resolving the showdown
     * <li>removing the loser's die (and possibly the loser!)
     * </ul>
     *
     * @throws Exception if there is a threading problem
     */
    private void playRound() throws Exception
    {
        Bid bid;

        logger.finest("inside playRound()");

        // initialize all variables for a new round
        prepareNewRound(currentPlayer);

        logger.fine("populated roundstate");
        logger.finest("roundstate has " + rs.getNumPlayers() + " number of players");

        // each bid
        do
        {
            logger.finest("new bid");

            // get the current player and their cup
            final var player = players.get(currentPlayer);
            final var cup = playerCups.get(currentPlayer);

            logger.finest("got cup for " + player.getClass().getSimpleName() + " " + cup.toString());

            // ask the player for a bid
            try
            {
                TimeoutSafePlayer tsplayer = new TimeoutSafePlayer(player, timeoutSeconds);
                bid = tsplayer.getBid(rs, new Cup(cup) /* give a tamper-proof copy of their cup */);
            }
            catch (Exception e)
            {
                logger.severe("Failed to construct a TimeoutSafePlayer");
                throw new Exception("Failed to construct a TimeoutSafePlayer", e);
            }

            logger.fine("Player: " + player.getClass().getSimpleName() + " bid " + bid);

            // if the bid is null, the player loses the round
            if (bid == null)
            {
                logger.warning(player.getClass().getSimpleName() + " returned a bid that was null");

                // punish loser
                punishLoser((currentPlayer + players.size() - 1) % players.size(), currentPlayer /* loser */);
                return;
            }
            else
            {
                bid.setPlayerNumDice(cup.getNumDice());
                bid.setPlayerSimpleClassName(player.getClass().getSimpleName());
            }

            // get previously highest bid
            Bid bidHighest = rs.getHighestBid();

            // add the bid to the round
            rs.addNextBid(bid);

            // inform each player of the new bid
            tellBid(rs);

            // validate bid
            // bid must outbid previous bid
            if (bidHighest != null)
            {
                // if bid is not higher, it is considered an automatic round loss
                if (bid.compareTo(bidHighest) < 1)
                {
                    logger.warning(player.getClass().getSimpleName() + " returned a bid that is too low");

                    // add the roundstate before we punish the loser
                    gamestate.add(rs);

                    // punish loser
                    punishLoser((currentPlayer + players.size() - 1) % players.size(), currentPlayer  /* loser */);
                    gamestate.add(rs);
                    return;
                }
            }

            // first bid cannot be bs
            if ((bidHighest == null) && (bid.isBS()))
            {
                logger.warning("First bid may not be b.s.");

                // add the roundstate before we punish the loser
                gamestate.add(rs);

                punishLoser((currentPlayer + players.size() - 1) % players.size(), currentPlayer  /* loser */);
                gamestate.add(rs);
                return;
            }

            // increment the player index
            currentPlayer = (currentPlayer + 1) % rs.getNumPlayers();

        }
        while (!bid.isBS());

        // add the roundstate before we punish the loser
        gamestate.add(rs);

        resolveShowdown();
    }

    /**
     * when one player has bid bs, this is called to resolve who is correct.
     */
    private void resolveShowdown() throws Exception
    {
        Bid bid;
        int iLoserIndex;
        int iWinnerIndex;

        try
        {
            bid = rs.getBid(rs.getNumBids() - 2);
        }
        catch (Exception e)
        {
            logger.severe("Failed to get bid to resolve showdown.");
            throw new Exception("Failed to get bid to resolve showdown.", e);
        }

        logger.finest("iCurrentPlayer: " + currentPlayer);

        final var iDefendingIndex = (currentPlayer + rs.getNumPlayers() - 2) % rs.getNumPlayers();
        final var iChallengingIndex = (currentPlayer + rs.getNumPlayers() - 1) % rs.getNumPlayers();

        logger.finest("Defender index: " + iDefendingIndex + " Challenger index: " + iChallengingIndex);

        // compare the most recent (non b.s.) bid to the actual cups
        if (bid.getNumDice() <= getNumDice(bid.getDots()))
        {
            iWinnerIndex = iDefendingIndex;
            iLoserIndex = iChallengingIndex;
        }
        else
        {
            iWinnerIndex = iChallengingIndex;
            iLoserIndex = iDefendingIndex;
        }

        punishLoser(iWinnerIndex, iLoserIndex);
    }

    /**
     * Tell everyone about the outcome of a showdown or an invalid play.
     * Remove a die from the loser.  Adjust the indices as necessary.
     *
     * @param iWinnerIndex int index of the winner in vPlayers for this round.
     * @param iLoserIndex  int index of the loser in vPlayers for this round.
     */
    private void punishLoser(int iWinnerIndex, int iLoserIndex)
    {
        // allow access to the cups
        rs.setShowdownOver();

        // tell everyone
        tellOutcome((players.get(iWinnerIndex)).getClass().getSimpleName(), (players.get(iLoserIndex)).getClass().getSimpleName());

        // remove a die from the losers cup
        (playerCups.get(iLoserIndex)).removeDie();
        currentPlayer = (iLoserIndex + 1) % rs.getNumPlayers();

        // remove the player and their cup if they have no dice left
        if ((playerCups.get(iLoserIndex)).getNumDice() == 0)
        {
            logger.info("goodbye: " + (players.get(iLoserIndex)).getClass().getSimpleName());
            players.remove(iLoserIndex);
            playerCups.remove(iLoserIndex);

            // should the current player index be adjusted?
            if (currentPlayer > iLoserIndex)
            {
                currentPlayer--;
            }
        }
    }

    /**
     * Based on the properties, instantiate each player's class.
     * Add each instance to a list.  The list is instantiated
     * if necessary, and it is cleared.  Then each player class
     * is added to the list
     *
     * @throws Exception On failure to create an instance of the player class.<br>
     *                   When not enough players are provided.
     */
    private void loadPlayers()
        throws Exception
    {
        String sPlayerClassName;
        int iPlayerNum = 0;

        logger.finest("loadPlayers() begin");

        // check to make sure that the list has been instantiated
        if (players == null)
        {
            players = new ArrayList<>();
        }

        logger.finest("clear list");
        players.clear();

        // populate the list with each of the player classes
        do
        {
            try
            {
                sPlayerClassName = System.getProperty("player" + iPlayerNum++);
            }
            catch (Exception e)
            {
                logger.severe("exception getting property");
                throw e;
            }

            logger.finest("PlayerClassName: " + sPlayerClassName);

            if (sPlayerClassName != null)
            {
                try
                {
                    final var cPlayerClass = java.lang.Class.forName(sPlayerClassName);
                    final var player = (Player) cPlayerClass.getDeclaredConstructor().newInstance();
                    players.add(player);

                    logger.fine("Added player: " + player.getClass().getSimpleName());
                }
                catch (Exception e)
                {
                    logger.severe("can't get class or new instance:" + sPlayerClassName);
                    throw e;
                }

            }  // if (sPlayerClassName != null)

        }
        while (sPlayerClassName != null);

        // there must be at least 2 players.
        if (players.size() < MIN_NUM_PLAYERS)
        {
            throw new Exception();
        }
    }

    /**
     * create a cup for each player.
     */
    private void makeCups()
    {
        // check to make sure that the list has been instantiated
        if (playerCups == null)
        {
            playerCups = new ArrayList<>();
        }

        logger.finest("clear player cups list");

        // clear the list
        playerCups.clear();

        // for each loaded player, make a cup
        int i = players.size();
        while (i-- > 0)
        {
            final var cup = new Cup(Cup.DEFAULT_NUM_DICE, Die.DEFAULT_NUM_SIDES);
            playerCups.add(cup);
        }
    }

    /**
     * shake all of the player's cups
     */
    private void shakeCups()
    {
        if (playerCups == null)
        {
            return;
        }

        // call shake for each cup
        for (Cup cup : playerCups)
        {
            cup.shake();
        }
    }

    /**
     * tell all of the players the state of the round
     * every time a bid is submitted.
     *
     * @param rs RoundState representing the player's dice position and the bid history
     *           for the current round.
     */
    private void tellBid(RoundState rs)
    {
        if (players == null)
        {
            return;
        }

        // call tellBid for each player
        for (Player player : players)
        {
            final var tsplayer = new TimeoutSafePlayer(player, timeoutSeconds);
            tsplayer.tellBid(rs);
        }
    }

    /**
     * Informs each player of the outcome of a showdown.
     *
     * @param sWinnerClassName String representing the email address of the winner of the showdown.
     * @param sLoserClassName  String representing the email address of the loser of the showdown.
     */
    private void tellOutcome(String sWinnerClassName, String sLoserClassName)
    {
        if (players == null)
        {
            return;
        }

        // call tellOutcome for each player
        for (Player player : players)
        {
            try
            {
                final var tsplayer = new TimeoutSafePlayer(player, timeoutSeconds);
                tsplayer.tellOutcome(rs, sWinnerClassName, sLoserClassName);
            }
            catch (Exception e)
            {
                // FIX - this method could propagate the exception
                logger.severe("failed to construct a TimeoutSafePlayer");
            }
        }
    }

    /**
     * A way to find out how many of a type of die you have in all cups.
     *
     * @param iDots int representing the rank of die you want to count.
     * @return int the number of dice that show iDots in all cups.
     */
    private int getNumDice(int iDots)
    {
        int iQuantity = 0;

        for (Cup cup : playerCups)
        {
            iQuantity += cup.getNumDice(iDots);
        }
        return iQuantity;
    }

    /**
     * Performs maintenance and initialization of variables in preparation of a new round.
     * Shakes the cups, chalks up the previous round, makes and initializes a new RoundState.
     *
     * @param iCurrentPlayer int representing an index of the current player
     */
    private void prepareNewRound(int iCurrentPlayer)
    {
        logger.fine("new round");

        // shake cups
        shakeCups();

        // append the round's roundstate to the GameState

        // make a RoundState for the round
        rs = new RoundState();
        logger.finest("created roundstate");

        // populate the RoundState
        int iIndex = iCurrentPlayer;
        int iCount = players.size();

        while (iCount-- > 0)
        {
            logger.finest("adding player to round state " + iIndex + " " + (players.get(iIndex)).getClass().getSimpleName());

            int iNumDice = (playerCups.get(iIndex)).getNumDice();
            rs.addPlayerState((players.get(iIndex)).getClass().getSimpleName(), iNumDice, new Cup(playerCups.get(iIndex)));
            iIndex = (iIndex + 1) % players.size();
        }
    }

    /**
     * setup the public static logger object.  Developers should use
     * a call to GameServer.   people should now use<br>
     * GameServer.logger to log their messages.
     */
    private static void setupLogger()
    {
        logger = Logger.getLogger(GameServer.class.getName());
        FileHandler fh = null;
        Level level;

        // get the property for logging level
        try
        {
            final var sLevel = System.getProperty(PROPERTY_DEBUG_LEVEL);

            // parse the string level into a Level object
            level = Level.parse(sLevel);
        }
        catch (Exception e)
        {
            // if no logging level is specified, use this level
            level = java.util.logging.Level.WARNING;
        }

        logger.setLevel(level);

        try
        {
            fh = new FileHandler(DEBUG_LOG);
            fh.setFormatter(new SimpleFormatter());
        }
        catch (Exception e)
        {
            System.out.println("Can't create log");
        }

        logger.addHandler(fh);
    }

    /**
     * Load system properties other than players.
     */
    private void loadOptions()
    {
        String timeoutSeconds = null;
        String numGames = null;

        // get the properties
        try
        {
            timeoutSeconds = System.getProperty(PROPERTY_TIMEOUT);
            numGames = System.getProperty(PROPERTY_NUMGAMES);
        }
        catch (Exception e)
        {
            logger.warning("exception getting property");
        }

        // set the timeout seconds member variable
        if (timeoutSeconds != null)
        {
            try
            {
                this.timeoutSeconds = Integer.parseInt(timeoutSeconds);
            }
            catch (NumberFormatException e)
            {
                logger.warning("unable to parse timeout:" + timeoutSeconds);
            }
        }

        // set the number of games member variable
        if (numGames != null)
        {
            try
            {
                this.numGames = Integer.parseInt(numGames);
            }
            catch (NumberFormatException e)
            {
                logger.warning("unable to parse numgames:" + numGames);
            }
        }
    }

    /**
     * Serialize the GameState to a file.  The GameViewer can read this to display the game.
     */
    private void saveGameState() throws Exception
    {
        // open file
        try(final var fos = new FileOutputStream(GameServer.GAME_LOG + getFormattedGameNumber() + "." + GameServer.GAME_LOG_EXT);
            final var oos = new ObjectOutputStream(fos);
            final var fos2 = new FileWriter(GameServer.GAME_LOG + getFormattedGameNumber() + ".json"))
        {
            oos.writeObject(gamestate);

            ObjectMapper mapper = new ObjectMapper();
            String s = mapper.writeValueAsString(gamestate);

            fos2.write(s);
        }
        catch (FileNotFoundException fnfe)
        {
            logger.severe("Unable to open a file to save the Game State");
            throw new Exception("Unable to open a file to save the Game State", fnfe);
        }
        catch (IOException ioe)
        {
            logger.severe("Unable to write the Game State to a file");
            throw new Exception("Unable to write the Game State to a file", ioe);
        }
    }

    private String getFormattedGameNumber()
    {
        return String.format("%05d", currentGameNumber);
    }
}
