package com.shuttersky.liarsdice.players;

import com.shuttersky.liarsdice.*;

public class PlayerBluffer implements Player
{
    public PlayerBluffer()
    {
        // do something here
    }

    public Bid getBid(RoundState rs, Cup cup)
    {
        GameServer.logger.entering("PlayerBluffer", "getBid");

        Bid bid = null;

        // get initial values
        final var iNumAllDice = rs.getNumDiceTotal();
        final var bidHighest = rs.getHighestBid();
        final var iNumSides = (cup.getDice().get(0)).getSides();

        // pick the dots at random
        final var dRnd = java.lang.Math.random();
        final var iDots = (int) (dRnd * iNumSides) + 1;

        // pick the quantity based on the number of dice remaining
        final var iQuantity = (iNumAllDice / iNumSides) + 1;

        // create a bid
        try
        {
            bid = new Bid(iQuantity, iDots);
        }
        catch (Exception e)
        {
        }

        // if the bid is invalid, call b.s.!
        if (bid.compareTo(bidHighest) < 1)
        {
            bid = new Bid();
        }

        GameServer.logger.finest("bluffer determined bid");

        return bid;
    }

    public void tellBid(RoundState rs)
    {
        // store this information to help determine your bid
    }

    public void tellOutcome(RoundState rs, String sWinnerEmail, String sLoserEmail)
    {
        // if this helps, store this information to use for future rounds
    }
}