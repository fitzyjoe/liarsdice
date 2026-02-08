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
    private enum Mode {UNKNOWN, GETBID, TELLBID, TELLOUTCOME}

    private final Player player;
    private Mode mode;
    private Cup cup;
    private RoundState rs;
    private String winnerClassName;
    private String loserClassName;
    private Bid bid;

    /**
     * Constructor.
     *
     * @param player Player that the PlayerCaller should invoke.
     */
    PlayerCaller(Player player)
    {
        this.player = player;
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
        switch (mode)
        {
            case GETBID -> {
                bid = null;
                bid = player.getBid(rs, cup);
            }
            case TELLBID -> player.tellBid(rs);
            case TELLOUTCOME -> player.tellOutcome(rs, winnerClassName, loserClassName);
            default -> {
            }
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
        mode = Mode.GETBID;
        this.cup = cup;
        this.rs = rs;
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
        mode = Mode.TELLBID;
        this.rs = rs;
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
        mode = Mode.TELLOUTCOME;
        this.rs = rs;
        this.winnerClassName = winnerClassName;
        this.loserClassName = loserClassName;
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
        return bid;
    }

    /**
     * Resets all of the member variables (except player) so that there is no residual data from a
     * previous call.
     */
    private void reset()
    {
        mode = Mode.UNKNOWN;
        cup = null;
        rs = null;
        winnerClassName = null;
        loserClassName = null;
        bid = null;
    }
}