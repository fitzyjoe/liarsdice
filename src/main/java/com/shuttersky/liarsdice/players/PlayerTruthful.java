package com.shuttersky.liarsdice.players;

import com.shuttersky.liarsdice.*;

import java.util.ArrayList;
import java.util.Iterator;

public class PlayerTruthful implements Player
{
    public PlayerTruthful()
    {
        // do something here
    }

    public Bid getBid(RoundState rs, Cup cup)
    {
        GameServer.logger.entering("PlayerTruthful", "getBid");

        Bid bid = null;
        Bid bidHighest = null;
        ArrayList<Bid> bids = cup.getSortedTrueBids();

        bidHighest = rs.getHighestBid();

        GameServer.logger.finest("Highest bid: " + bidHighest);

        // am I the first bidder this round?
        if (bidHighest == null)
        {
            return bids.get(0);
        }

        Iterator<Bid> iterBids = bids.iterator();
        if (iterBids.hasNext() == false)
        {
            GameServer.logger.finest("call to getSortedTrueBids returned null");
            System.exit(-1);
        }

        bid = iterBids.next();

        GameServer.logger.finest("looping through bids");

        // escalate the bid formulation until it is higher than the highest bid for this round
        while (bid.compareTo(bidHighest) < 1)
        {
            if (iterBids.hasNext())
            {
                bid = iterBids.next();
            }
            else
            {
                bid = new Bid();
            }
        }

        if (bid.isBS())
        {
            return new Bid("Show me the money");
        }

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
