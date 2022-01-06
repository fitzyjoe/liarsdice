
package com.shuttersky.liarsdice;

/**
 * A class representing a die, which by default will have
 * 6 sides, however the number of sides is configurable.
 * As you'd expect the dice can be rolled, but this method
 * is protected so that players can not roll their own dice.
 * Instead they must rely on the game server to roll
 * their dice.
 */
public class Die implements java.io.Serializable, Comparable<Die>
{
    /**
     * for serializable
     */
    public static final long serialVersionUID = 1;

    /**
     * The default number of sides for a die.
     */
    public static final int DEFAULT_NUM_SIDES = 6;

    /**
     * bounds check for a die
     */
    private static final int MIN_SIDES = 2;

    /**
     * Member variables for the number of sides on a die.
     * and the number of dots on the indicated side
     */
    private int _sides;
    private int _dots;


    /**
     * constructor.  This will use the default number of sides.
     */
    public Die()
    {
        this._sides = DEFAULT_NUM_SIDES;
        roll();
    }


    /**
     * constructor.  This constructor allows a variable number of sides.
     *
     * @param iSides int representing the number of sides that the die
     *               should have.
     * @throws java.lang.Exception
     */
    public Die(int iSides)
        throws Exception
    {
        if (iSides < MIN_SIDES)
        {
            throw new Exception("Die must have at least 2 sides");
        }

        this._sides = iSides;
        roll();
    }

    protected Die(Die die)
    {
        this._dots = die._dots;
        this._sides = die._sides;
    }


    /**
     * this rolls the dice and may assign a new number to dots.
     * The number ranges from 1 to the number of sides.
     * This method is protected so that players may
     * not roll their own dice.
     */
    protected void roll()
    {
        double d = java.lang.Math.random();
        _dots = (int) (d * _sides) + 1;
    }

    /**
     * This allows the gameServer to copy dice objects.
     *
     * @param dots
     */
    protected void setDots(int dots)
    {
        _dots = dots;
    }


    /**
     * returns the number of dots on the selected face.
     *
     * @return int representing the number of dots on the selected face.
     */
    public int getDots()
    {
        return _dots;
    }


    /**
     * returns the number of sides on a die
     *
     * @return int representing the number of sides on a die
     */
    public int getSides()
    {
        return _sides;
    }

    public int compareTo(Die die)
        throws ClassCastException
    {
        if (_dots < die.getDots())
        {
            return -1;
        }
        else if (_dots > die.getDots())
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }


}
