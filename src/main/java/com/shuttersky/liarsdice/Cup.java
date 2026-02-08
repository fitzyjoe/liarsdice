package com.shuttersky.liarsdice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/**
 * The Cup class serves the purpose to contain dice, just as it does
 * in real life.  Developers can use the cup to determine how many
 * dice are left, as well as to obtain a copy of the list of dice
 * contained in the cup.
 */
public class Cup implements java.io.Serializable
{
    /**
     * for serializable
     */
    public static final long serialVersionUID = 1;

    /**
     * The default number of dice in a cup.
     */
    public static final int DEFAULT_NUM_DICE = 5;

    /**
     * member list representing the dice in the cup.
     */
    private final ArrayList<Die> dice;

    /**
     * constructor.  After creating the specified number of dice with the
     * correct number of sides, the dice are sorted;
     *
     * @param iNumDice  int representing the number of dice to create in the cup.
     * @param iNumSides int representing the number of sides each die in the
     *                  cup should have.
     */
    public Cup(int iNumDice, int iNumSides)
    {
        Die die;

        dice = new ArrayList<>();

        for (int i = 0; i < iNumDice; i++)
        {
            die = new Die(iNumSides);
            dice.add(die);
        }

        Collections.sort(dice);
    }

    /**
     * copy constructor.
     *
     * @param cup A Cup to be copied.
     */
    public Cup(Cup cup)
    {
        // declare a new array for the cup
        dice = new ArrayList<>(cup.getNumDice());

        for (Die die : cup.dice)
        {
            dice.add(new Die(die));
        }

        Collections.sort(dice);
    }

    /**
     * Get the number of dice in the cup.
     *
     * @return The number of dice in the cup
     */
    public int getNumDice()
    {
        return dice.size();
    }

    /**
     * Randomize all of the dice in the cup.  Resort the dice.
     */
    protected void shake()
    {
        for (Die die : dice)
        {
            die.roll();
        }

        Collections.sort(dice);
    }

    /**
     * Get a list containing the dice in the cup
     *
     * @return a copy of the list that contains the dice in the cup.
     */
    public ArrayList<Die> getDice()
    {
        return new ArrayList<>(dice);
    }

    /**
     * Take one die away from the cup.  This method removes the die
     * at index 0.  The list is resized and all indicies shift.  An
     * exception is thrown if you removeDie from an empty cup.
     */
    protected void removeDie()
    {
        if (dice.size() < 1)
        {
            throw new RuntimeException("no dice to remove");
        }
        dice.remove(0);
    }

    /**
     * Probably only useful as a design aid, this returns an exhaustive
     * list of truthful bids that may be created from the contents of this cup.<P>
     * <p>
     * Example: if a cup holds 1, 3, 3, 6<br>
     * then it would return sorted bids:<br>
     * (quantity, dots) (1, 1), (1, 3), (1, 6), (2, 3)
     * <p>
     * Note: The internal logic assumes that the dice in the cup are sorted.
     *
     * @return ArrayList containing the bids composed from the contents of the cup.
     */
    public ArrayList<Bid> getSortedTrueBids()
    {
        final var bids = new ArrayList<Bid>();
        Bid bid = null;
        int dots1 = 0;

        // create an iterator over the sorted dice
        Iterator<Die> iter = dice.iterator();

        // create a bid for the first die
        if (iter.hasNext())
        {
            dots1 = (iter.next()).getDots();
            bid = new Bid(1, dots1);
            bids.add(bid);
            GameServer.logger.finest("getSortedTrueBids: " + bid);
        }

        // iterate over the rest of the dice
        while (iter.hasNext())
        {
            // get the number of dots
            final var dots2 = (iter.next()).getDots();

            if (dots2 == dots1)
            {
                // dots are the same, increment the bid.
                bid = new Bid(bid.getNumDice() + 1, dots1);
                bids.add(bid);
                GameServer.logger.finest("getSortedTrueBids: " + bid);
            }
            else
            {
                // dots are different, save the bid and start a new bid
                bid = new Bid(1, dots2);
                bids.add(bid);
                GameServer.logger.finest("getSortedTrueBids: " + bid);
                dots1 = dots2;
            }
        }

        Collections.sort(bids);

        return bids;
    }


    /**
     * A way to find out how many of a type of die you have in the cup.
     * The dice are assumed to be sorted.
     *
     * @param dots int representing the rank of die you want to count.
     * @return int the number of dice that show dots in the cup.
     */
    public int getNumDice(int dots)
    {
        int quantity = 0;

        for (Die die : dice)
        {
            if (dots == die.getDots())
            {
                quantity++;
            }
        }
        return quantity;
    }

    @Override
    public String toString()
    {
        String s;

        // if the cup is empty set the string to empty
        if (dice.size() == 0)
        {
            s = "empty";
        }
        else
        {
            // comma separate the contents of the cup
            final var sb = new StringBuilder();
            for (Die die : dice)
            {
                sb.append(die.getDots()).append(", ");
            }

            // remove the trailing comma
            sb.setLength(sb.length() - 2);
            s = sb.toString();
        }

        return s;
    }
}