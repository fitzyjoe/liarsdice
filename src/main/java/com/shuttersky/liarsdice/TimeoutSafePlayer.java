
package com.shuttersky.liarsdice;

/**
 * TimeoutSafePlayer solves the problem of the GameServer blocking for
 * a Player that does not respond (or that does not respond fast enough)
 */
class TimeoutSafePlayer implements com.shuttersky.liarsdice.Player
{
    /**
     * A bounds checking constant for the smallest allowable timeout
     */
    private static final int MIN_TIMEOUT_SECONDS = 0;

    /**
     * PlayerCaller is the runnable class that calls the player.
     */
    private PlayerCaller _playerCaller;

    /**
     * timeoutSeconds is the maximum number of seconds to wait for the playerCaller
     * to return from its run method.
     */
    private int _timeoutSeconds;

    /**
     * constructor creates a new instance of a TimeoutSafePlayer.
     *
     * @param player         Player that the playerCaller should call.
     * @param timeoutSeconds int representing the maximum number of seconds to wait
     *                       for the playerCaller to return from its run method.
     * @throws java.lang.Exception if the timeout seconds is negative
     */
    TimeoutSafePlayer(Player player, int timeoutSeconds)
        throws Exception
    {
        if (timeoutSeconds < MIN_TIMEOUT_SECONDS)
        {
            throw new Exception("TimeoutSafePlayer constuctor exception.  timeoutSeconds less than minimum bounds");
        }

        this._playerCaller = new PlayerCaller(player);
        this._timeoutSeconds = timeoutSeconds;

        GameServer.logger.finest("TimeoutSafePlayer made for: " + player.toString() + " with timeout seconds: " + timeoutSeconds);
    }

    /**
     * Give your bid to the GameServer.
     *
     * @param cup This is your cup.  Unless you want to go blind, look
     * @param rs  Roundstate capturing the state of the table for this round.
     * @return A Bid to the GameServer when it is requested.
     * <code>null</code> if player does not return a bid within the time limit
     */
    public Bid getBid(RoundState rs, Cup cup)
    {
        _playerCaller.setModeGetBid(rs, cup);
        Thread playerCallerThread = new Thread(_playerCaller);
        playerCallerThread.start();

        try
        {
            playerCallerThread.join(_timeoutSeconds * 1000);
        }
        catch (Exception e)
        {
            // logging a warning is all that is necessary, because this is a result
            // of a player not responding to telling them the outcome of a round
            GameServer.logger.warning("TimetouSafePlayer.getBid join exception.  player class interrupted.");
        }

        // return the result of the thread... it should be null if the thread has not finished
        return _playerCaller.getBid();

    }

    /**
     * The GameServer will call this every time a player bids.
     *
     * @param rs RoundState capturing the state of the table for this round.
     */
    public void tellBid(RoundState rs)
    {
        _playerCaller.setModeTellBid(rs);
        Thread playerCallerThread = new Thread(_playerCaller);
        playerCallerThread.start();

        try
        {
            playerCallerThread.join(_timeoutSeconds * 1000);
        }
        catch (Exception e)
        {
            // logging a warning is all that is necessary, because this is a result
            // of a player not responding to telling them the outcome of a round
            GameServer.logger.warning("TimetouSafePlayer.tellBid join exception.  player class interrupted.");
        }
    }

    /**
     * The GameServer tells each player of the outcome of a showdown.
     *
     * @param winnerClassName String representing the class name of the showdown winner.
     * @param loserClassName  String representing the class name of the showdown loser.
     */
    public void tellOutcome(RoundState rs, String winnerClassName, String loserClassName)
    {
        _playerCaller.setModeTellOutcome(rs, winnerClassName, loserClassName);
        Thread playerCallerThread = new Thread(_playerCaller);
        playerCallerThread.start();

        try
        {
            playerCallerThread.join(_timeoutSeconds * 1000);
        }
        catch (Exception e)
        {
            // logging a warning is all that is necessary, because this is a result
            // of a player not responding to telling them the outcome of a round
            GameServer.logger.warning("TimeoutSafePlayer.tellOutcome join exception.  player class interrupted.");
        }

    }

}
