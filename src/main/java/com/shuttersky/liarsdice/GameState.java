
package com.shuttersky.liarsdice;

import java.util.HashMap;
import java.util.ArrayList;


/**
 * This object contains all information about the state of a game.
 * <p>
 * Note: this is package level
 */
class GameState extends java.util.ArrayList<RoundState> implements java.io.Serializable
{
    /**
     * for serializable
     */
    public static final long serialVersionUID = 1;

    // a poor man's id for the game
    private java.util.Date _bornOnDate = null;

    protected GameState()
    {
        // call the parent's constructor
        super();

        // set the born on date
        _bornOnDate = new java.util.Date();
    }

    /**
     * Get the index number of a player, by name
     *
     * @param simpleClassName String simple class name of a player
     * @return the index of the player in the first round
     * @throws Exception if the player is not found in this round
     */
    protected int getPlayerIndex(String simpleClassName) throws Exception
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
        HashMap<String, Integer> playerClassNames = new HashMap<String, Integer>();
        ArrayList<String> orderedPlayerClassNames = new ArrayList<String>();
        int iNumPlayers = 0;
        int iNumRound = this.size();
        int iIndexRound = 0;
        int iIndexPlayer = 0;
        Object o = null;
        String playerClassName = null;

        // for each round, put the playerClassName in a hashmap with the round
        // this will give us a list of players along with their highest round

        // for each round in reverse
        for (iIndexRound = iNumRound - 1; iIndexRound >= 0; iIndexRound--)
        {
            // get the number of players for that round
            iNumPlayers = (this.get(iIndexRound)).getNumPlayers();

            // loop over each player
            for (iIndexPlayer = 0; iIndexPlayer < iNumPlayers; iIndexPlayer++)
            {
                try
                {
                    // get the playerClassName
                    playerClassName = (this.get(iIndexRound)).getPlayerSimpleClassName(iIndexPlayer);
                }
                catch (Exception e)
                {
                    // this will never happen
                }

                // put the player in the hashmap
                o = playerClassNames.put(playerClassName, Integer.valueOf(iIndexRound));

                // is the player new to the hashmap?
                if (o == null)
                {
                    // there was no previous mapping for this playerClassName
                    orderedPlayerClassNames.add(playerClassName);
                }
            }
        }

        GameServer.logger.info("Game born on date: " + _bornOnDate.toString());
        GameServer.logger.info("Winner to loser order for game " + formattedGameNumber + ": " + orderedPlayerClassNames.toString());
    }

    /**
     * override Vector.clear method to clear the born on date
     */
    public void clear()
    {
        _bornOnDate = null;
        super.clear();
    }

    /**
     * override Vector.removeAllElements method to clear the born on date
     */
    protected void removeAllElements()
    {
        _bornOnDate = null;
        super.clear();
    }


}
