package com.shuttersky.liarsdice;

/**
 * This class represents a player's bid in the liars dice game.
 * A player bids a quantity of dice and a number of dots on one side.
 * The other bid that a player may make is b.s.
 * When the game server asks for a bid, the user will return this object
 * to the gameserver.
 */
public class Bid implements Comparable<Bid>, java.io.Serializable
{
    /**
     * for serializable
     */
    public static final long serialVersionUID = 1;

    /**
     * constants defining the bounds of a bid.
     */
    private static final int MIN_QUANTITY = 1;
    private static final int MIN_DOTS = 1;
    private static final int MAX_DOTS = 6;

    /**
     * member variables holding the state of a bid.
     */
    private int quantity;
    private int dots;
    private boolean bBS;
    private String playerSimpleClassName = null;
    private int playerNumDice = 0;
    private String message = null;

    /**
     * Copy constructor
     *
     * @param bid The bid to copy
     */
    protected Bid(Bid bid)
    {
        quantity = bid.quantity;
        dots = bid.dots;
        bBS = bid.bBS;
        playerSimpleClassName = bid.playerSimpleClassName;
        playerNumDice = bid.playerNumDice;
        message = bid.message;
    }

    /**
     * constructor.
     *
     * @param quantity int representing the quantity of dice bid.
     * @param dots     int representing the number of dots on one side of a die in the bid.
     * @param message  String for the message (trash-talk) to show in the game viewer.
     */
    public Bid(int quantity, int dots, String message)
    {
        // set the values for the bid
        setBid(quantity, dots);
        this.message = message;
        bBS = false;
    }

    /**
     * constructor.
     *
     * @param quantity int representing the quantity of dice bid.
     * @param dots     int representing the number of dots on one side of a die in the bid.
     */
    public Bid(int quantity, int dots)
    {
        setBid(quantity, dots);
        bBS = false;
    }

    /**
     * constructor for creating a b.s. bid.
     *
     * @param message String for the message (trash-talk) to show in the game viewer.
     */
    public Bid(String message)
    {
        setBid(Bid.MIN_QUANTITY, Bid.MIN_DOTS);
        this.message = message;
        bBS = true;
    }

    /**
     * constructor for creating a b.s. bid.
     */
    public Bid()
    {
        setBid(Bid.MIN_QUANTITY, Bid.MIN_DOTS);
        bBS = true;
    }

    /**
     * Get the message (trash-talk) for a bid.
     *
     * @return the message for a bid.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * determine if a bid is b.s.
     *
     * @return <code>true</code> if the bid is b.s.
     * <code>false</code> otherwise.
     */
    public boolean isBS()
    {
        return bBS;
    }

    /**
     * get the quantity of dice in the bid.
     *
     * @return int representing the number of dice in the bid.
     * <code>-1</code> if bid is b.s.
     */
    public int getNumDice()
    {
        if (bBS)
        {
            return -1;
        }

        return quantity;
    }

    /**
     * get the number of dots on one die in the bid.
     *
     * @return int representing the number of dots on one die in the bid.
     * <code>-1</code> if bid is bs
     */
    public int getDots()
    {
        if (bBS)
        {
            return -1;
        }

        return dots;
    }

    /**
     * Get the simple class name of the player who placed this bid.  This is set by the GameServer
     *
     * @return String representing the simple class name of the player who
     * placed this bid.
     */
    public String getPlayerSimpleClassName()
    {
        return playerSimpleClassName;
    }

    /**
     * Get the number of dice that are being held by the player who placed this bid.
     * This is set by the GameServer
     *
     * @return int representing the number of dice that are being held by the
     * player who placed this bid.
     */
    public int getPlayerNumDice()
    {
        return playerNumDice;
    }

    /**
     * This compares two bids.
     *
     * @param bid A Bid to compare
     * @return <code>-1</code> if this is less than bid.
     * <code>1</code> if this is greater than bid. Or if the bid is null.
     * <code>0</code> if the bids are equal.
     */
    @Override
    public int compareTo(Bid bid)
    {
        if (bid == null)
        {
            return 1;
        }

        // compare b.s. state
        if (isBS() || bid.isBS())
        {
            if (isBS() && bid.isBS())
            {
                // both bids are b.s.
                return 0;
            }
            else if (isBS())
            {
                // this bid is b.s.
                return 1;
            }
            else
            {
                // the passed in bid is b.s.
                return -1;
            }
        }

        // compare quantities
        if (quantity > bid.getNumDice())
        {
            // this has larger quantity than bid
            return 1;
        }
        else if (quantity < bid.getNumDice())
        {
            // this has less quantity than bid
            return -1;
        }
        else
        {
            // both bids have the same quantity
            // now check the dots
            return Integer.compare(dots, bid.getDots());
        }
    }

    /**
     * A String representation of a bid: Quantity + "x" + Dots + "'s"
     *
     * @return a String representation of a bid.
     */
    @Override
    public String toString()
    {
        String s;

        if (isBS())
        {
            s = "b.s.!";
        }
        else
        {
            s = quantity + "x" + dots + "'s";
        }

        return s;
    }

    /**
     * The game server sets the player class name as a convenience for players
     *
     * @param playerSimpleClassName the class name of a player
     */
    protected void setPlayerSimpleClassName(String playerSimpleClassName)
    {
        this.playerSimpleClassName = playerSimpleClassName;
    }

    /**
     * The game server sets the player number of dice as a convenience for players
     *
     * @param playerNumDice int number of dice for a player associated with this bid
     */
    protected void setPlayerNumDice(int playerNumDice)
    {
        this.playerNumDice = playerNumDice;
    }

    /**
     * set a bid to the given value.
     *
     * @param quantity int representing the quantity of dice bid.
     * @param dots     int representing the number of dots on one side of a die in the bid.
     */
    private void setBid(int quantity, int dots)
    {
        setQuantity(quantity);
        setDots(dots);
        bBS = false;
    }

    /**
     * set the quantity of dice in the bid.
     *
     * @param quantity int representing the number of dice in the bid.
     */
    private void setQuantity(int quantity)
    {
        // validate quantity
        if (quantity < MIN_QUANTITY)
        {
            throw new RuntimeException("Attempted to set the quantity to: " + quantity + " which is lower than the minimum: " + MIN_QUANTITY);
        }

        // set quantity
        this.quantity = quantity;
    }

    /**
     * set the bid for the number of dots on one side of the dice.
     * The dots must be between <code>MIN_DOTS</code> and <code>MAX_DOTS</code>
     *
     * @param dots int representing the number of dots on one side of the dice in the bid.
     */
    private void setDots(int dots)
    {
        // validate dots
        if (dots < MIN_DOTS || dots > MAX_DOTS)
        {
            throw new RuntimeException("Dots " + dots + " is out of range");
        }

        // set dots
        this.dots = dots;
    }
}