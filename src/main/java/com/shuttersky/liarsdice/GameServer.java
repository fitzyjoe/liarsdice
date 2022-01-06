
package com.shuttersky.liarsdice;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.Locale;


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
     * the timeout for each player can by configured by defining this property
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
    private ArrayList<Player> _players = null;
    private ArrayList<Cup> _playerCups = null;
    private RoundState _rs = null;
    private GameState _gamestate = null;
    private int _currentPlayer = 0;
    private int _timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private int _numGames = DEFAULT_NUMGAMES;
    private int _currentGameNumber = 0;

    /**
     * static logger object.  This should be used by all objects in the game.
     */
    public static Logger logger = null;

    /**
     * this gets executed when the GameServer is instantiated.
     */
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

        while (gs._currentGameNumber < gs._numGames)
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
                gs._gamestate.logResults(gs.getFormattedGameNumber());

                // serialize the gamestate for the game viewer
                gs.saveGameState();
            }
            catch (Exception e)
            {
                logger.severe("Unable to save the game to a file");
                System.exit(-1);
            }

            gs._currentGameNumber++;
        }

        logger.exiting("GameServer", "main");
    }


    /**
     * Plays one game of liars dice.
     */
    private void PlayGame() throws Exception
    {
        // create a gamestate to keep statistics
        _gamestate = new GameState();

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
        logger.finest("number of players: " + _players.size());

        // while there is more than one player
        while (_players.size() > 1)
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

        logger.info("The winner is " + (_players.get(0)).getClass().getSimpleName());
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
        Bid bid = null;
        Player player = null;
        Cup cup = null;

        logger.finest("inside playRound()");

        // initialize all variables for a new round
        prepareNewRound(_currentPlayer);

        logger.fine("populated roundstate");

        logger.finest("roundstate has " + _rs.getNumPlayers() + " number of players");

        // each bid
        do
        {
            logger.finest("new bid");

            // get the current player and their cup
            player = _players.get(_currentPlayer);
            cup = _playerCups.get(_currentPlayer);

            logger.finest("got cup for " + player.getClass().getSimpleName() + " " + cup.toString());

            // ask the player for a bid
            try
            {
                TimeoutSafePlayer tsplayer = new TimeoutSafePlayer(player, _timeoutSeconds);
                bid = tsplayer.getBid(_rs, new Cup(cup) /* give a tamper-proof copy of their cup */);
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
                punishLoser((_currentPlayer + _players.size() - 1) % _players.size(), _currentPlayer /* loser */);
                return;
            }

            if (bid != null)
            {
                bid.setPlayerNumDice(cup.getNumDice());
                bid.setPlayerSimpleClassName(player.getClass().getSimpleName());
            }

            // get previously higest bid
            Bid bidHighest = _rs.getHighestBid();

            // add the bid to the round
            _rs.addNextBid(bid);

            // inform each player of the new bid
            tellBid(_rs);

            // validate bid
            // bid must outbid previous bid
            if (bidHighest != null)
            {
                // if bid is not higher, it is considered an automatic round loss
                if (bid.compareTo(bidHighest) < 1)
                {
                    logger.warning(player.getClass().getSimpleName() + " returned a bid that is too low");

                    // add the roundstate before we punish the loser
                    _gamestate.add(_rs);

                    // punish loser
                    punishLoser((_currentPlayer + _players.size() - 1) % _players.size(), _currentPlayer  /* loser */);
                    _gamestate.add(_rs);
                    return;
                }
            }

            // first bid cannot be bs
            if ((bidHighest == null) && (bid.isBS()))
            {
                logger.warning("First bid may not be b.s.");

                // add the roundstate before we punish the loser
                _gamestate.add(_rs);

                punishLoser((_currentPlayer + _players.size() - 1) % _players.size(), _currentPlayer  /* loser */);
                _gamestate.add(_rs);
                return;
            }

            // increment the player index
            _currentPlayer = (_currentPlayer + 1) % _rs.getNumPlayers();

        }
        while (bid.isBS() != true);

        // add the roundstate before we punish the loser
        _gamestate.add(_rs);

        resolveShowdown();
    }


    /**
     * when one player has bid bs, this is called to resolve who is correct.
     */
    private void resolveShowdown() throws Exception
    {
        Bid bid = null;
        int iDefendingIndex = 0;
        int iChallengingIndex = 0;
        int iLoserIndex = 0;
        int iWinnerIndex = 0;

        try
        {
            bid = _rs.getBid(_rs.getNumBids() - 2);
        }
        catch (Exception e)
        {
            logger.severe("Failed to get bid to resolve showdown.");
            throw new Exception("Failed to get bid to resolve showdown.", e);
        }

        logger.finest("iCurrentPlayer: " + _currentPlayer);

        iDefendingIndex = (_currentPlayer + _rs.getNumPlayers() - 2) % _rs.getNumPlayers();
        iChallengingIndex = (_currentPlayer + _rs.getNumPlayers() - 1) % _rs.getNumPlayers();

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
        _rs.setShowdownOver();

        // tell everyone
        tellOutcome((_players.get(iWinnerIndex)).getClass().getSimpleName(), (_players.get(iLoserIndex)).getClass().getSimpleName());

        // remove a die from the losers cup
        try
        {
            (_playerCups.get(iLoserIndex)).removeDie();
        }
        catch (Exception e)
        {
            logger.severe("bad cup index");
            System.exit(-1);
        }
        _currentPlayer = (iLoserIndex + 1) % _rs.getNumPlayers();

        // remove the player and their cup if they have no dice left
        if ((_playerCups.get(iLoserIndex)).getNumDice() == 0)
        {
            logger.info("goodbye: " + (_players.get(iLoserIndex)).getClass().getSimpleName());
            _players.remove(iLoserIndex);
            _playerCups.remove(iLoserIndex);

            // should the current player index be adjusted?
            if (_currentPlayer > iLoserIndex)
            {
                _currentPlayer--;
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
        String sPlayerClassName = null;
        Class cPlayerClass = null;
        Player player = null;
        int iPlayerNum = 0;

        logger.finest("loadPlayers() begin");

        // check to make sure that the list has been instantiated
        if (_players == null)
        {
            _players = new ArrayList<Player>();
        }

        logger.finest("clear list");
        _players.clear();

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
                    cPlayerClass = java.lang.Class.forName(sPlayerClassName);
                    player = (Player) cPlayerClass.newInstance();
                    _players.add(player);

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
        if (_players.size() < MIN_NUM_PLAYERS)
        {
            throw new Exception();
        }


    }


    /**
     * create a cup for each player.
     */
    private void makeCups()
        throws Exception
    {
        Cup cup = null;

        // check to make sure that the list has been instantiated
        if (_playerCups == null)
        {
            _playerCups = new ArrayList<Cup>();
        }

        logger.finest("clear player cups list");

        // clear the list
        _playerCups.clear();

        // for each loaded player, make a cup
        int i = _players.size();
        while (i-- > 0)
        {
            try
            {
                cup = new Cup(Cup.DEFAULT_NUM_DICE, Die.DEFAULT_NUM_SIDES);
            }
            catch (Exception e)
            {
                //this will never happen since we're using Cup's static variable to initialize it.
                logger.severe("Could not create a new cup");
                System.exit(-1);
            }

            _playerCups.add(cup);
        }
    }


    /**
     * shake all of the player's cups
     */
    private void shakeCups()
    {
        if (_playerCups == null)
        {
            return;
        }

        // call shake for each cup
        for (Cup cup : _playerCups)
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
        if (_players == null)
        {
            return;
        }

        // call tellBid for each player
        for (Player player : _players)
        {
            try
            {
                TimeoutSafePlayer tsplayer = new TimeoutSafePlayer(player, _timeoutSeconds);
                tsplayer.tellBid(rs);
            }
            catch (Exception e)
            {
                // This should never happen
                logger.severe("failed to construct a TimeoutSafePlayer");
                System.exit(-1);
            }

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
        if (_players == null)
        {
            return;
        }

        // call tellOutcome for each player
        for (Player player : _players)
        {
            try
            {
                TimeoutSafePlayer tsplayer = new TimeoutSafePlayer(player, _timeoutSeconds);
                tsplayer.tellOutcome(_rs, sWinnerClassName, sLoserClassName);
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

        for (Cup cup : _playerCups)
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
        _rs = new RoundState();
        logger.finest("created roundstate");

        // populate the RoundState
        int iIndex = iCurrentPlayer;
        int iCount = _players.size();

        while (iCount-- > 0)
        {
            logger.finest("adding player to round state " + iIndex + " " + (_players.get(iIndex)).getClass().getSimpleName());

            int iNumDice = (_playerCups.get(iIndex)).getNumDice();
            _rs.addPlayerState((_players.get(iIndex)).getClass().getSimpleName(), iNumDice, new Cup(_playerCups.get(iIndex)));
            iIndex = (iIndex + 1) % _players.size();
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
        String sLevel = null;
        Level level = null;

        // get the property for logging level level
        try
        {
            sLevel = System.getProperty(PROPERTY_DEBUG_LEVEL);

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
                _timeoutSeconds = Integer.parseInt(timeoutSeconds);
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
                _numGames = Integer.parseInt(numGames);
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
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try
        {
            fos = new FileOutputStream(GameServer.GAME_LOG + getFormattedGameNumber() + "." + GameServer.GAME_LOG_EXT);

            oos = new ObjectOutputStream(fos);

            oos.writeObject(_gamestate);
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
        StringBuilder paddedGameNumber = new StringBuilder();
        Formatter logfileFormatter = new Formatter(paddedGameNumber, Locale.US);
        logfileFormatter.format("%05d", _currentGameNumber);
        return paddedGameNumber.toString();
    }
}
