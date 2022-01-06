
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
    private int _quantity;
    private int _dots;
    private boolean _bBS = false;
    private String _playerSimpleClassName = null;
    private int _playerNumDice = 0;
    private String _message = null;

    /**
     * Copy constructor
     *
     * @param bid The bid to copy
     */
    protected Bid(Bid bid)
    {
        _quantity = bid._quantity;
        _dots = bid._dots;
        _bBS = bid._bBS;
        _playerSimpleClassName = bid._playerSimpleClassName;
        _playerNumDice = bid._playerNumDice;
        _message = bid._message;
    }


    /**
     * constructor.
     *
     * @param quantity int representing the quantity of dice bid.
     * @param dots     int representing the number of dots on one side of a die in the bid.
     * @param message  String for the message (trash-talk) to show in the game viewer.
     * @throws Exception if the bid is out of range.
     */
    public Bid(int quantity, int dots, String message)
        throws Exception
    {
        // set the values for the bid
        setBid(quantity, dots);
        _message = message;
        _bBS = false;
    }


    /**
     * constructor.
     *
     * @param quantity int representing the quantity of dice bid.
     * @param dots     int representing the number of dots on one side of a die in the bid.
     * @throws Exception if the bid is out of range.
     */
    public Bid(int quantity, int dots)
        throws Exception
    {
        setBid(quantity, dots);
        _bBS = false;
    }


    /**
     * constructor for creating a b.s. bid.
     *
     * @param message String for the message (trash-talk) to show in the game viewer.
     */
    public Bid(String message)
    {
        try
        {
            setBid(Bid.MIN_QUANTITY, Bid.MIN_DOTS);
        }
        catch (Exception e)
        {
            // no need to handle exception since we're using bounds constants
            // to initialize the Bid
        }

        _message = message;
        _bBS = true;
    }


    /**
     * constructor for creating a b.s. bid.
     */
    public Bid()
    {
        try
        {
            setBid(Bid.MIN_QUANTITY, Bid.MIN_DOTS);
        }
        catch (Exception e)
        {
            // no need to handle exception since we're using bounds constants
            // to initialize the Bid
        }

        _bBS = true;
    }


    /**
     * Get the message (trash-talk) for a bid.
     *
     * @return the message for a bid.
     */
    public String getMessage()
    {
        return _message;
    }


    /**
     * determine if a bid is b.s.
     *
     * @return <code>true</code> if the bid is b.s.
     * <code>false</code> otherwise.
     */
    public boolean isBS()
    {
        return _bBS;
    }


    /**
     * get the quantity of dice in the bid.
     *
     * @return int representing the number of dice in the bid.
     * <code>-1</code> if bid is b.s.
     */
    public int getNumDice()
    {
        if (_bBS == true)
        {
            return -1;
        }

        return _quantity;
    }


    /**
     * get the number of dots on one die in the bid.
     *
     * @return int representing the number of dots on one die in the bid.
     * <code>-1</code> if bid is bs
     */
    public int getDots()
    {
        if (_bBS == true)
        {
            return -1;
        }

        return _dots;
    }


    /**
     * Get the simple class name of the player who placed this bid.  This is set by the GameServer
     *
     * @return String representing the simple class name of the player who
     * placed this bid.
     */
    public String getPlayerSimpleClassName()
    {
        return _playerSimpleClassName;
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
        return _playerNumDice;
    }


    /**
     * This compares two bids.
     *
     * @param bid A Bid to compare
     * @return <code>-1</code> if this is less than bid.
     * <code>1</code> if this is greater than bid. Or if the bid is null.
     * <code>0</code> if the bids are equal.
     * @throws ClassCastException if obid is not a bid
     */
    public int compareTo(Bid bid)
        throws ClassCastException
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
        if (_quantity > bid.getNumDice())
        {
            // this has larger quantity than bid
            return 1;
        }
        else if (_quantity < bid.getNumDice())
        {
            // this has less quantity than bid
            return -1;
        }
        else
        {
            // both bids have the same quantity
            // now check the dots
            if (_dots > bid.getDots())
            {
                // this has more dots than bid
                return 1;
            }
            else if (_dots < bid.getDots())
            {
                // this has less dots than bid
                return -1;
            }
            else
            {
                return 0;
            }
        }
    }


    /**
     * A String representation of a bid: Quantity + "x" + Dots + "'s"
     *
     * @return a String representation of a bid.
     */
    public String toString()
    {
        String s = null;

        if (isBS() == true)
        {
            s = "b.s.!";
        }
        else
        {
            s = String.valueOf(_quantity) + "x" + String.valueOf(_dots) + "'s";
        }

        return s;
    }


    /**
     * The game server sets the player class name as a convenience for players
     *
     * @param playerSimpleClassName
     */
    protected void setPlayerSimpleClassName(String playerSimpleClassName)
    {
        _playerSimpleClassName = playerSimpleClassName;
    }


    /**
     * The game server sets the player number of dice as a convenience for players
     *
     * @param playerNumDice
     */
    protected void setPlayerNumDice(int playerNumDice)
    {
        _playerNumDice = playerNumDice;
    }


    /**
     * set a bid to the given value.
     *
     * @param quantity int representing the quantity of dice bid.
     * @param dots     int representing the number of dots on one side of a die in the bid.
     * @throws Exception if the bid is out of range.
     */
    private void setBid(int quantity, int dots)
        throws Exception
    {
        setQuantity(quantity);
        setDots(dots);

        _bBS = false;
    }


    /**
     * set the quantity of dice in the bid.
     *
     * @param quantity int representing the number of dice in the bid.
     * @throws Exception if the quantity is out of range
     */
    private void setQuantity(int quantity)
        throws Exception
    {
        // validate quantity
        if (quantity < MIN_QUANTITY)
        {
            throw new Exception("Attempted to set the quantity to: " + quantity + " which is lower than the minimum: " + MIN_QUANTITY);
        }

        // set quantity
        this._quantity = quantity;
    }


    /**
     * set the bid for the number of dots on one side of the dice
     *
     * @param dots int representing the number of dots on one side of the dice in the bid.
     * @throws Exception if the dots are out of range
     */
    private void setDots(int dots)
        throws Exception
    {
        // validate dots
        if (dots < MIN_DOTS || dots > MAX_DOTS)
        {
            throw new Exception("Dots " + dots + " is out of range");
        }

        // set dots
        this._dots = dots;
    }

}
