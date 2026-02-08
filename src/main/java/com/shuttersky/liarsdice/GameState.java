package com.shuttersky.liarsdice;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * This object contains all information about the state of a game.
 */
class GameState extends java.util.ArrayList<RoundState> implements java.io.Serializable
{
    /**
     * for serializable
     */
    public static final long serialVersionUID = 1;

    // a poor man's id for the game
    private java.util.Date bornOnDate;

    protected GameState()
    {
        // call the parent's constructor
        super();

        // set the born on date
        bornOnDate = new java.util.Date();
    }

    /**
     * Get the index number of a player, by name
     *
     * @param simpleClassName String simple class name of a player
     * @return the index of the player in the first round
     * @throws RuntimeException if the player is not found in this round
     */
    protected int getPlayerIndex(String simpleClassName)
    {
        return this.get(0).getPlayerIndex(simpleClassName);
    }

    /**
     * Get the number of players in round 0
     *
     * @return the number of players in round 0
     */
    protected int getNumPlayers()
    {
        return this.get(0).getNumPlayers();
    }

    protected void logResults(String formattedGameNumber)
    {
        HashMap<String, Integer> playerClassNames = new HashMap<>();
        ArrayList<String> orderedPlayerClassNames = new ArrayList<>();
        int iNumRound = this.size();

        // for each round, put the playerClassName in a hashmap with the round
        // this will give us a list of players along with their highest round

        // for each round in reverse
        for (int iIndexRound = iNumRound - 1; iIndexRound >= 0; iIndexRound--)
        {
            // get the number of players for that round
            final var iNumPlayers = (this.get(iIndexRound)).getNumPlayers();

            // loop over each player
            for (int iIndexPlayer = 0; iIndexPlayer < iNumPlayers; iIndexPlayer++)
            {
                // get the playerClassName
                final var playerClassName = (this.get(iIndexRound)).getPlayerSimpleClassName(iIndexPlayer);

                // put the player in the hashmap
                final var o = playerClassNames.put(playerClassName, iIndexRound);

                // is the player new to the hashmap?
                if (o == null)
                {
                    // there was no previous mapping for this playerClassName
                    orderedPlayerClassNames.add(playerClassName);
                }
            }
        }

        GameServer.logger.info("Game born on date: " + bornOnDate.toString());
        GameServer.logger.info("Winner to loser order for game " + formattedGameNumber + ": " + orderedPlayerClassNames);
    }

    /**
     * override clear() method to clear the born on date
     */
    @Override
    public void clear()
    {
        bornOnDate = null;
        super.clear();
    }
}