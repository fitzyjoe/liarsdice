Liar's Dice
-----------

Sections:

*   House rules for a real-life game
*   Computer adaptation of the rules
*   How to run the game
*   Related info
*   Development history
*   License

### House rules for a real-life game

1.  Each player is given 5 dice and a cup.
2.  Each player rolls 1 dice, and the lowest roller bids first.
3.  Each player shakes their dice and the first bidder bids.  
    *   A quantity of dice
    *   A dots valueFor example the bid might be "three fours." This bid encompasses all of the players' dice. In other words this bid means that among all of the players in the game, the bidding player believes that there are "three or more fours" when all players combine their dice.
4.  The next player must either bid higher or call b.s.
5.  A higher bid would include either
    *   The same quantity and a higher dots value; or
    *   A higher quantity and any dots value.
6.  A player calls b.s. by lifting up their cup. All players lift their cups, and the showdown is decided.
7.  The loser of the showdown loses one of their dice and displays it in front of them for counting purposes.
8.  Play continues with a bid from the person to the left of the loser.
9.  A player with no dice left is eliminated from the game.
10.  The game ends when there is one player left.
11.  If a roll includes stacked dice, the player lifts their cup to reveal the stack, and then re-rolls all dice.

### Computer adaptation of the rules

The following are differences in the rules based on the computer adaptation:

*   The player indicated by property "player0" bids first.
*   If a player returns an invalid bid, they lose a die.
*   A player can only take a specified number of seconds to respond to a getBid request.

### How to run the game

Currently this game runs under Java 1.5 or better. If you run it on the command line, you should use the ant script `(ant playgame)` or do something like this:

`java -cp lib/liarsdice.jar;lib/sampleplayers.jar;lib/swing-layout-1.0.2.jar  
     -Dplayer0=_YourFirstPlayerClassName_  
     -Dplayer1=_YourSecondPlayerClassName_  
     -Dplayer_n_=_YourNthPlayerClassName_  
     [ -Ddebuglevel=_FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE_ ]  
     [ -Dtimeout=_number of seconds_ ]  
     [ -Dnumgames=_number of games_ ]  
     com.shuttersky.liarsdice.GameServer`

Included in the liarsdice.jar is a GUI player that you can use to play against the computer players. To use the GUI, specify this class as one of the players: `com.shuttersky.liarsdice.players.PlayerSwing`

A debug log named `debug.log` is maintained. Also, at the end of the game, the details of the game are serialized to another file called `game[gamenum].log`. To review the events of the game, run the game viewer with the ant script `(ant viewgame)` or like this:

`java -cp lib/liarsdice.jar;lib/sampleplayers.jar;lib/swing-layout-1.0.2.jar com.shuttersky.liarsdice.GameViewer`

The first thing you see is a file open dialog. Browse to the working directory and select a game log to view the results of that game.

### Related info

For more external info check out the following:

*   [Google: liar's dice](http://www.google.com/search?hl=en&ie=UTF-8&oe=UTF-8&q=%22liar%27s+dice%22)

### Development history

10/13/2002  
Liar's Dice was written to serve as a programming contest, allowing each player to develop their own Player Class that is used by the GameServer to pit against each other. At this point the intent is to play computer against computer, but in the future it may be adapted to fit a user interface.

11/05/2002  
PlayerSwing class created. When this player is included, a UI allows interactive play with other computer Player classes.

07/12/2007  
Updated code with Java 1.5 features. Lots of improvements. Added game viewer.

01/06/2022  
Updated for Java 17 Moved from ant to gradle Moved from NetBeans to IntelliJ
