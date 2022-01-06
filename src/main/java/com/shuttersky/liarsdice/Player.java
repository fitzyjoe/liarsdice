
package com.shuttersky.liarsdice;


/**
 * Players will extend this class to implement their own logic to formulate a bid.
 * Class names are used as a unique indicator of the player.<P>
 * <ul><li>If it is your turn, the game server will call the <code>getBid</code> method</li>
 * <li>Every time there is a bid, the game server will call the <code>tellBid</code> method of every player.</li>
 * <li>In the case of a showdown among any players in the game, the gameserver will decide
 * the winner and inform all players by calling the <code>tellOutcome</code> method.</li></ul><P>
 * <p>
 * To log messages, use GameServer.logger.log( yourlevel, yourmessage );
 */
public interface Player
{
    /**
     * Give your bid to the GameServer.
     *
     * @param cup This is your cup.  Unless you want to go blind, look
     * @param rs  Roundstate capturing the state of the table for this round.
     * @return A Bid to the GameServer when it is requested.
     */
    public abstract Bid getBid(RoundState rs, Cup cup);


    /**
     * The GameServer will call this every time a player bids.
     *
     * @param rs RoundState capturing the state of the table for this round.
     */
    public abstract void tellBid(RoundState rs);


    /**
     * The GameServer tells each player of the outcome of a showdown.
     *
     * @param rs               RoundState capturing the state of the table for this round.  At this point, access to the cups is allowed!
     * @param sWinnerClassName String representing the class name of the showdown winner.
     * @param sLoserClassName  String representing the class name of the showdown loser.
     */
    public abstract void tellOutcome(RoundState rs, String sWinnerClassName, String sLoserClassName);

}
