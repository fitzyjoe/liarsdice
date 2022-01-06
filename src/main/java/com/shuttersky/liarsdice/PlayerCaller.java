
package com.shuttersky.liarsdice;

/**
 * PlayerCaller is a runnable class that is started by the TimeoutSafePlayer, and is used
 * to interact with the Player class to ensure that the GameServer is not affected
 * in the case when a Player hangs or takes too long.
 * <p>
 * The calling class should set the mode of a PlayerCaller and then use start() to
 * invoke PlayerCaller.
 */
class PlayerCaller implements java.lang.Runnable
{
    private enum Mode
    {UNKNOWN, GETBID, TELLBID, TELLOUTCOME}

    private Player _player;
    private Mode _mode;
    private Cup _cup;
    private RoundState _rs;
    private String _winnerClassName;
    private String _loserClassName;
    private Bid _bid;


    /**
     * Constructor.
     *
     * @param player Player that the PlayerCaller should invoke.
     */
    PlayerCaller(Player player)
    {
        _player = player;
        reset();
    }


    /**
     * run() determines which method to call on the player, calls it, then sets any
     * member variables if necessary.
     * <p>
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread#run()
     */
    public void run()
    {
        // determine which method to run
        switch (_mode)
        {
            case GETBID:
                _bid = null;
                _bid = _player.getBid(_rs, _cup);
                break;
            case TELLBID:
                _player.tellBid(_rs);
                break;
            case TELLOUTCOME:
                _player.tellOutcome(_rs, _winnerClassName, _loserClassName);
                break;
            default:
                break;
        }
    }

    /**
     * setModeGetBid sets member variables to prepare the PlayerCaller to call the player.  After calling
     * this method, it is expected that start() will be called on this thread once.
     *
     * @param cup This is the player's cup.
     * @param rs  Roundstate capturing the state of the table for this round.
     */
    protected void setModeGetBid(RoundState rs, Cup cup)
    {
        reset();
        _mode = Mode.GETBID;
        _cup = cup;
        _rs = rs;
    }

    /**
     * setModeTellBid sets member variables to prepare the PlayerCaller to call the player.  After calling
     * this method, it is expected that start() will be called on this thread once.
     *
     * @param rs RoundState capturing the state of the table for this round.
     */
    protected void setModeTellBid(RoundState rs)
    {
        reset();
        _mode = Mode.TELLBID;
        _rs = rs;
    }

    /**
     * setModeTellOutcome sets member variables to prepare the PlayerCaller to call the player.  After calling
     * this method, it is expected that start() will be called on this thread once.
     *
     * @param rs              RoundState capturing the state of the table for this round.  At this point the cups are accessable.
     * @param winnerClassName String representing the class name of the showdown winner.
     * @param loserClassName  String representing the class name of the showdown loser.
     */
    protected void setModeTellOutcome(RoundState rs, String winnerClassName, String loserClassName)
    {
        reset();
        _mode = Mode.TELLOUTCOME;
        _rs = rs;
        _winnerClassName = winnerClassName;
        _loserClassName = loserClassName;
    }

    /**
     * getBid provides access to the bid member variable.  It is expected that the internal developer will use this class by
     * <ol>
     * <li>Calling setModeGetBid
     * <li>Starting a thread for PlayerCaller
     * <li>Joining back with the thread
     * <li>Calling getBid to determine the results of the thread
     * </ol>
     *
     * @return Bid representing the results of a call to setModeGetBid
     */
    protected Bid getBid()
    {
        return _bid;
    }

    /**
     * Resets all of the member variables (except player) so that there is no residual data from a
     * previous call.
     */
    private void reset()
    {
        _mode = Mode.UNKNOWN;
        _cup = null;
        _rs = null;
        _winnerClassName = null;
        _loserClassName = null;
        _bid = null;
    }
}
