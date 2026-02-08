
package com.shuttersky.liarsdice;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * This object contains all information about the state of a round.
 * It is passed to a player when their bid is requested.  They can use
 * information contained in this object to formulate their bid.
 */
public class RoundState implements java.io.Serializable
{
    /**
     * for serializable
     */
    public static final long serialVersionUID = 1;

    /**
     * Player class name and number of dice are added to this list
     * in bid order starting with the first bid at index 0.
     */
    final private ArrayList<String> playerSimpleClassNames;
    final private ArrayList<Integer> numDice;
    final private ArrayList<Bid> bids;
    final private ArrayList<Cup> cups;

    private int numDiceTotal;
    private boolean isShowdownOver;

    /**
     * constructor
     */
    public RoundState()
    {
        playerSimpleClassNames = new ArrayList<>();
        numDice = new ArrayList<>();
        bids = new ArrayList<>();
        cups = new ArrayList<>();
        isShowdownOver = false;
    }

    /**
     * Tells how many players are in this round.
     *
     * @return int representing the number of players are in this round.
     */
    public int getNumPlayers()
    {
        return playerSimpleClassNames.size();
    }

    /**
     * Get the index number of a player, by name
     *
     * @param simpleClassName String simple class name of a player
     * @return the index of the player
     * @throws RuntimeException if the player is not found in this round
     */
    public int getPlayerIndex(String simpleClassName)
    {
        final var index = playerSimpleClassNames.indexOf(simpleClassName);

        if (index == -1)
        {
            throw new RuntimeException();
        }

        return index;
    }

    /**
     * Get the simpleClassName of the player at <code>index</code> seat at the table.
     *
     * @param index the index of the player for this round where 0 would be
     *              considered to be the first player and the index is modulo by the number
     *              of players so that this index may be the same as the bid index.
     *              <p>
     *              Note:
     *              <ol><li>As players lose, they are not present in the following round</li>
     *              <li>The index of players may change from round to round as the first player
     *              for the round is placed first in the array</li></ol>
     * @return String the simple class name for the user at <code>index</code>.
     */
    public String getPlayerSimpleClassName(int index)
    {
        index %= getNumPlayers();

        return playerSimpleClassNames.get(index);
    }

    /**
     * Allow a player to have read-only access to a list of player class names
     * Note:
     *
     * <ol><li>As players lose, they are not present in the following round</li>
     * <li>The index of players may change from round to round as the first player
     * for the round is placed first in the array</li></ol>
     *
     * @return List&lt;String&gt; of player simple class names
     */
    public List<String> getPlayerSimpleClassNames()
    {
        return Collections.unmodifiableList(playerSimpleClassNames);
    }

    /**
     * tells how many bids have been submitted for this round.
     *
     * @return int representing the number of bids submitted for this round.
     */
    public int getNumBids()
    {
        return bids.size();
    }

    /**
     * Get the bid that was submitted in index order.
     *
     * @param index ranging from 0 to <code>getNumBids()</code>-1 submitted for this round.
     *              0 would be considered to be the first bid.
     * @return Bid that was submitted at index order
     * @throws RuntimeException if index is out of range.
     */
    public Bid getBid(int index)
    {
        // index out of range
        if (index < 0 || index > getNumBids() - 1)
        {
            throw new RuntimeException("index " + index + " is out of range in getBid");
        }

        return new Bid(bids.get(index));
    }

    /**
     * Allow a player to have read-only access to a list of bids.
     *
     * @return List&lt;Bid&gt; of bids.
     */
    public List<Bid> getBids()
    {
        return Collections.unmodifiableList(bids);
    }

    /**
     * returns the highest (most recent) bid.
     *
     * @return Bid the highest (most recent) for this round.
     * If there are no bids for this round, this
     * method returns <code>null</code>.
     */
    public Bid getHighestBid()
    {
        GameServer.logger.entering("RoundState", "getHighestBid");

        Bid bid;

        if (bids.size() == 0)
        {
            return null;
        }

        try
        {
            bid = bids.get(bids.size() - 1);
        }
        catch (java.util.NoSuchElementException e)
        {
            GameServer.logger.warning("getHighestBid was unable to determine the highest bid. " + e.getMessage());
            return null;
        }

        return new Bid(bid);
    }

    /**
     * Get the number of dice for the player at <code>index</code>.
     *
     * @param index the index of the player for this round where 0 would be
     *              considered to be the first player and the index is modulo by the number
     *              of players so that this index may be the same as the bid index.
     * @return the number of dice for the player at <code>index</code> seat.
     */
    public int getNumDice(int index)
    {
        // mod the index to keep it in bounds
        index %= getNumPlayers();

        return numDice.get(index).intValue();
    }

    /**
     * Allow a player to have read-only access to a list of number of dice.
     *
     * @return List&lt;Integer&gt; of number of dice for each player.
     */
    public List<Integer> getNumDice()
    {
        return Collections.unmodifiableList(numDice);
    }

    /**
     * Get the number of dice for the player requested.
     *
     * @param simpleClassName String representing the simpleClassName of a player class
     * @return int representing the number of dice that <code>simpleClassName</code>
     * has for the current round.
     * @throws RuntimeException if <code>simpleClassName</code> is not currently playing.
     */
    public int getNumDice(String simpleClassName)
    {
        int index = playerSimpleClassNames.indexOf(simpleClassName);

        if (index == -1)
        {
            throw new RuntimeException(simpleClassName + " not found in current round");
        }

        return numDice.get(index).intValue();
    }

    /**
     * tells the number of dice remaining for this round.
     *
     * @return int representing the number of dice remain for this round.
     */
    public int getNumDiceTotal()
    {
        return numDiceTotal;
    }

    /**
     * Once the showdown is over, a user may learn by inspecting all of the other
     * player's cups.
     *
     * @param index the index of the player for this round where 0 would be
     *              considered to be the first player and the index is modulo by the number
     *              of players so that this index may be the same as the bid index.
     * @return a copy of the cup for the player at <code>index</code> seat.
     * @throws RuntimeException if this method is called before the showdown.
     */
    public Cup getCup(int index)
    {
        if (!isShowdownOver)
        {
            throw new RuntimeException("attempted to access a cup before the end of the showdown");
        }

        // mod the index to keep it in bounds
        index %= getNumPlayers();

        return new Cup(cups.get(index));
    }

    /**
     * Once the showdown is over, allow a player to have read-only access to a list of Cups.
     *
     * @return List&lt;Cup&gt; of Cups for each player.
     * @throws RuntimeException if this method is called before the showdown
     */
    public List<Cup> getCups()
    {
        if (!isShowdownOver)
        {
            throw new RuntimeException("attempted to access a cup before the end of the showdown");
        }

        return Collections.unmodifiableList(cups);
    }

    /**
     * Get the Cup for the player requested.
     *
     * @param simpleClassName String representing the simpleClassName of a player class
     * @return Cup representing the cup of dice that <code>simpleClassName</code>
     * has for the current round.
     * @throws RuntimeException if <code>simpleClassName</code> is not currently playing
     *                   or if this method is called before the showdown
     */
    public Cup getCup(String simpleClassName)
    {
        if (!isShowdownOver)
        {
            throw new RuntimeException("attempted to access a cup before the end of the showdown");
        }

        var index = playerSimpleClassNames.indexOf(simpleClassName);

        if (index == -1)
        {
            throw new RuntimeException(simpleClassName + " player not found in current round");
        }

        return new Cup(cups.get(index));
    }

    /*
     * String representation of a RoundState.
     *
     * @return String representation of a RoundState.
     */
    @Override
    public String toString()
    {
        final var sb = new StringBuffer();
        int index = 0;

        sb.append("PLAYERS\n");

        while (index < getNumPlayers())
        {
            sb.append(playerSimpleClassNames.get(index));
            sb.append(" has ");
            var intobjNumDice = numDice.get(index);
            sb.append(intobjNumDice.toString());
            sb.append(" dice\n");
            index++;
        }

        sb.append("BID HISTORY\n");

        index = 0;
        while (index < getNumBids())
        {
            sb.append(bids.get(index).getPlayerSimpleClassName());
            sb.append("\t");
            sb.append(bids.get(index));
            sb.append("\n");
            index++;
        }

        return sb.toString();
    }

    /**
     * The GameServer uses this to initialize the state of the round.
     * This populates the member variables to associate the simpleClassName
     * of the players with the number of dice they have
     * left as well as their bid order
     *
     * @param simpleClassName String representing the simpleClassName of the player
     * @param iNumDice        int representing the number of dice the player has left
     * @param cup             The cup for a player is not made public until tell outcome
     */
    protected void addPlayerState(final String simpleClassName, final int iNumDice, final Cup cup)
    {
        playerSimpleClassNames.add(simpleClassName);
        final var intobjNumDice = Integer.valueOf(iNumDice);
        numDice.add(intobjNumDice);
        numDiceTotal += iNumDice;
        cups.add(new Cup(cup));  // make a new one because the gameserver modifies the passed in cup from round to round
    }

    /**
     * used by the GameServer to add each bid that is cast.
     *
     * @param bid The next bid that is cast within the round.
     */
    protected void addNextBid(Bid bid)
    {
        bids.add(bid);
    }

    /**
     * Used by the GameServer to allow access to the cups
     */
    protected void setShowdownOver()
    {
        isShowdownOver = true;
    }

    /**
     * Get the Cup for the player requested.
     *
     * @param simpleClassName String representing the simpleClassName of a player class
     * @return Cup representing the cup of dice that <code>simpleClassName</code>
     * has for the current round.
     * @throws RuntimeException if <code>simpleClassName</code> is not currently playing
     */
    protected Cup getCupProtected(String simpleClassName)
    {
        int index = playerSimpleClassNames.indexOf(simpleClassName);

        if (index == -1)
        {
            throw new RuntimeException(simpleClassName + " player is not playing in this round");
        }

        return new Cup(cups.get(index));
    }
}