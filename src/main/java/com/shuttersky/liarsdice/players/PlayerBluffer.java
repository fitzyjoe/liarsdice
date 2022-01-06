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
        Bid bidHighest = null;
        int iNumAllDice = 0;
        int iNumSides = 0;
        int iQuantity = 0;
        int iDots = 0;
        double dRnd = 0;

        // get initial values
        iNumAllDice = rs.getNumDiceTotal();
        bidHighest = rs.getHighestBid();
        iNumSides = (cup.getDice().get(0)).getSides();

        // pick the dots at random
        dRnd = java.lang.Math.random();
        iDots = (int) (dRnd * iNumSides) + 1;

        // pick the quantity based on the number of dice remaining
        iQuantity = (iNumAllDice / iNumSides) + 1;

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
        // store this information so to help determine your bid
    }

    public void tellOutcome(RoundState rs, String sWinnerEmail, String sLoserEmail)
    {
        // if this helps, store this information to use for future rounds
    }


}
