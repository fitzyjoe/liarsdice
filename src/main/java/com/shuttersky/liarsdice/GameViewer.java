/*
 * GameViewer.java
 *
 */

package com.shuttersky.liarsdice;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;


public class GameViewer extends javax.swing.JFrame implements ActionListener
{
    /**
     * for serializable
     */
    public static final long serialVersionUID = 1;

    private static final int PLAYER_0_TOP = 200;

    private static final int PLAYER_HEIGHT = 74;

    private static final Color COLOR_DISABLED = new java.awt.Color(153, 153, 153);

    private static final Color COLOR_ENABLED = new java.awt.Color(0, 0, 0);

    private static final Color COLOR_HIGHLIGHTED = new java.awt.Color(0, 102, 0);

    private static final Font DEFAULT_FONT = new java.awt.Font("Tahoma", 0, 11);

    private GameState _gameState = null;

    private GameViewer _gameViewer = null;

    private ArrayList<PlayerUI> _playerUI = null;

    private int _bidIndex = 0;

    private int _roundIndex = 0;

    private ArrayList<Integer> _countDice = new ArrayList<Integer>(Die.DEFAULT_NUM_SIDES);

    /**
     * Creates new form GameViewer
     */
    public GameViewer()
    {
        // open a file for viewing
        String selectedFile = selectLogFile();

        // load the Game State
        try
        {
            _gameState = loadGameState(selectedFile);
        }
        catch (Exception e)
        {
            System.out.println("Unable to load a Game State.  Exiting");
            System.exit(-1);
        }

        // netbeans generated init
        initComponents();

        // my own init
        initComponents2();

        this._gameViewer = this;
    }

    private void initComponents2()
    {
        getContentPane().setBackground(Color.WHITE);

        _playerUI = new ArrayList<PlayerUI>();

        // make it easy to access the player user interface elements
        _playerUI.add(new PlayerUI(panelPlayer0, labelCup0, labelPlayerCup0, labelBid0, labelPlayerBid0));
        _playerUI.add(new PlayerUI(panelPlayer1, labelCup1, labelPlayerCup1, labelBid1, labelPlayerBid1));
        _playerUI.add(new PlayerUI(panelPlayer2, labelCup2, labelPlayerCup2, labelBid2, labelPlayerBid2));
        _playerUI.add(new PlayerUI(panelPlayer3, labelCup3, labelPlayerCup3, labelBid3, labelPlayerBid3));
        _playerUI.add(new PlayerUI(panelPlayer4, labelCup4, labelPlayerCup4, labelBid4, labelPlayerBid4));
        _playerUI.add(new PlayerUI(panelPlayer5, labelCup5, labelPlayerCup5, labelBid5, labelPlayerBid5));
        _playerUI.add(new PlayerUI(panelPlayer6, labelCup6, labelPlayerCup6, labelBid6, labelPlayerBid6));
        _playerUI.add(new PlayerUI(panelPlayer7, labelCup7, labelPlayerCup7, labelBid7, labelPlayerBid7));

        // associate actions
        buttonExit.addActionListener(this);
        buttonNext.addActionListener(this);

        // set an icon for the frame
        ImageIcon imgIcon = new ImageIcon("dice.gif");
        setIconImage(imgIcon.getImage());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            return;
        }

        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                GameViewer gv = new GameViewer();

                gv.updateUI();

                // resize the window
                gv.setHeightForPlayers(gv._gameState.getNumPlayers());

                gv.setVisible(true);

                return;

            }
        });
    }

    /**
     * Serialize the GameState to a file
     */
    private static final GameState loadGameState(String selectedLogFile) throws Exception
    {
        // open file
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        GameState gameState = null;

        try
        {
            fis = new FileInputStream(selectedLogFile);

            ois = new ObjectInputStream(fis);

            gameState = (GameState) ois.readObject();
        }
        catch (FileNotFoundException fnfe)
        {
            System.out.println("Unable to open a file to load the Game State " + GameServer.GAME_LOG);
            throw new Exception("Unable to open a file to load the Game State " + GameServer.GAME_LOG, fnfe);
        }
        catch (IOException ioe)
        {
            System.out.println("Unable to read the Game State file " + GameServer.GAME_LOG);
            throw new Exception("Unable to read the Game State file " + GameServer.GAME_LOG, ioe);
        }

        return gameState;
    }

    private void disablePlayer(int playerIndex)
    {
        // get the UI elements that we need to disable
        PlayerUI playerUI = _playerUI.get(playerIndex);

        String playerName = _gameState.get(0).getPlayerSimpleClassName(playerIndex);

        playerUI.getPanelPlayer().setBorder(javax.swing.BorderFactory.createTitledBorder(null, playerName, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, DEFAULT_FONT, COLOR_DISABLED));
        playerUI.getLabelCup().setForeground(COLOR_DISABLED);
        playerUI.getLabelPlayerCup().setForeground(COLOR_DISABLED);
        playerUI.getLabelBid().setForeground(COLOR_DISABLED);
        playerUI.getLabelPlayerBid().setForeground(COLOR_DISABLED);
    }


    private void clearAllPlayers()
    {
        int numPlayers = _gameState.getNumPlayers();

        while (numPlayers-- > 0)
        {
            updatePlayer(numPlayers, "", "");
        }
    }

    private void disableAllPlayers()
    {
        int numPlayers = _gameState.getNumPlayers();

        while (numPlayers-- > 0)
        {
            disablePlayer(numPlayers);
        }
    }


    private void updatePlayer(int playerIndex, String labelPlayerCup, String labelPlayerBid)
    {
        // get the UI elements that we need to disable
        PlayerUI playerUI = _playerUI.get(playerIndex);

        playerUI.getLabelPlayerCup().setText(labelPlayerCup);
        playerUI.getLabelPlayerBid().setText(labelPlayerBid);
    }


    private void enablePlayer(int playerIndex)
    {
        try
        {
            // get the UI elements that we need to disable
            PlayerUI playerUI = _playerUI.get(playerIndex);

            // the requested player's class name
            String playerName = _gameState.get(0).getPlayerSimpleClassName(playerIndex);

            // the current bidder's class name
            String bidPlayerName = _gameState.get(_roundIndex).getBid(_bidIndex).getPlayerSimpleClassName();

            int currentPlayerIndex = _gameState.getPlayerIndex(bidPlayerName);


            Color borderColor = (playerIndex == currentPlayerIndex) ? COLOR_HIGHLIGHTED : COLOR_ENABLED;

            playerUI.getPanelPlayer().setBorder(javax.swing.BorderFactory.createTitledBorder(null, playerName, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, DEFAULT_FONT, borderColor));
            playerUI.getLabelCup().setForeground(COLOR_ENABLED);
            playerUI.getLabelPlayerCup().setForeground(COLOR_ENABLED);
            playerUI.getLabelBid().setForeground(COLOR_ENABLED);
            playerUI.getLabelPlayerBid().setForeground(borderColor);
        }
        catch (Exception e)
        {
        }
    }

    private void setHeightForPlayers(int numPlayers)
    {
        _gameViewer.setSize(400, PLAYER_0_TOP + PLAYER_HEIGHT * numPlayers);
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == buttonNext)
        {
            next();
        }
        //        else if (e.getSource() == buttonPrev)
        //        {
        //            prev();
        //        }
        else if (e.getSource() == buttonExit)
        {
            exit();
        }
    }

    private void exit()
    {
        System.exit(0);
    }

    public void next()
    {
        // is there another bid in the round?
        if (_bidIndex < _gameState.get(_roundIndex).getNumBids() - 1)
        {
            _bidIndex++;
        }
        else if (_roundIndex < _gameState.size() - 1)
        {
            // do we need to move to the next round?
            _roundIndex++;
            _bidIndex = 0;
        }
        else
        {
            // we are at the end of the game
        }

        updateUI();
    }
    
    /*
    private void prev()
    {
        // is there an earlier bid in the round?
        if (_bidIndex > 0)
        {
            _bidIndex--;
        }
        else if (_roundIndex > 0)
        {
            // is there an earlier round in the game?
            _roundIndex--;
        }
        else
        {
            // we are at the beginning of the game
        }
    }
     */

    private String selectLogFile()
    {
        JFileChooser chooser = new JFileChooser();
        LogFileFilter filter = new LogFileFilter();
        chooser.setFileFilter(filter);

        int fileSelectionResult = chooser.showOpenDialog(this);

        if (fileSelectionResult != JFileChooser.APPROVE_OPTION)
        {
            System.exit(-1);
        }

        return chooser.getSelectedFile().getName();
    }

    private void updateUI()
    {
        // show the breakdown of all of the dice
        java.util.List<String> playerNames = _gameState.get(_roundIndex).getPlayerSimpleClassNames();
        String bidAnalysis = null;

        try
        {
            // if we're at the beginning of a round
            if (_bidIndex == 0)
            {
                StringBuffer statsEachDie = new StringBuffer();

                // initialize _countDice
                _countDice.clear();
                for (int i = 0; i < Die.DEFAULT_NUM_SIDES; i++)
                {
                    _countDice.add(Integer.valueOf(0));
                }

                // clear the bids and cups
                clearAllPlayers();

                // refresh the status
                labelStatsNumDice.setText(_gameState.get(_roundIndex).getNumDiceTotal() + "");

                // disable all players
                disableAllPlayers();

                // enable the current players
                for (String playerName : playerNames)
                {
                    int playerIndex = _gameState.getPlayerIndex(playerName);
                    PlayerUI playerUI = _playerUI.get(playerIndex);
                    Cup cup = _gameState.get(_roundIndex).getCupProtected(playerName);
                    playerUI.getLabelPlayerCup().setText(cup.toString());

                    // count up all of the dice
                    for (Die die : cup.getDice())
                    {
                        Integer countDie = _countDice.get(die.getDots() - 1);
                        countDie = countDie + 1;
                        _countDice.set(die.getDots() - 1, countDie);
                    }
                }

                // display the counts of each die
                for (int dotIndex = 0; dotIndex < Die.DEFAULT_NUM_SIDES; dotIndex++)
                {
                    statsEachDie.append(_countDice.get(dotIndex).toString() + "x" + (dotIndex + 1) + "'s ");
                }

                labelStatsEachDie.setText(statsEachDie.toString());
            }

            // draw the players in the proper color
            for (String playerName : playerNames)
            {
                int playerIndex = _gameState.getPlayerIndex(playerName);
                enablePlayer(playerIndex);
            }

            // update the current bid
            Bid bid = _gameState.get(_roundIndex).getBid(_bidIndex);
            String playerClassName = bid.getPlayerSimpleClassName();
            int playerIndex = _gameState.getPlayerIndex(playerClassName);
            PlayerUI playerUI = _playerUI.get(playerIndex);

            // is the bid good, risky, good bs, or bad bs?
            if (bid.isBS())
            {
                // bs
                // get previous bid for comparison
                Bid prevBid = _gameState.get(_roundIndex).getBid(_bidIndex - 1);

                // was the previous bid good or bad?
                if (prevBid.getNumDice() <= _countDice.get(prevBid.getDots() - 1))
                {
                    // previous bid was good
                    bidAnalysis = " - Loser";
                }
                else
                {
                    // previous bid was not good
                    bidAnalysis = " - Nice call";
                }
            }
            else
            {
                // non bs
                // is the current bid good or bad?
                if (bid.getNumDice() <= _countDice.get(bid.getDots() - 1))
                {
                    // current bid is good
                    bidAnalysis = " - good bid";
                }
                else
                {
                    // current bid is not good
                    bidAnalysis = " - risky bid";
                }
            }


            playerUI.getLabelPlayerBid().setText(bid.toString() + bidAnalysis);

            // udpate trash talk
            labelStatsTrashTalk.setText(bid.getMessage());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }


    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        buttonNext = new javax.swing.JButton();
        buttonExit = new javax.swing.JButton();
        javax.swing.JPanel panelStats = new javax.swing.JPanel();
        javax.swing.JLabel labelNumDice = new javax.swing.JLabel();
        labelStatsNumDice = new javax.swing.JLabel();
        labelStatsTrashTalk = new javax.swing.JLabel();
        javax.swing.JLabel labelTrashTalk = new javax.swing.JLabel();
        javax.swing.JLabel labelEachDie = new javax.swing.JLabel();
        labelStatsEachDie = new javax.swing.JLabel();
        panelPlayer0 = new javax.swing.JPanel();
        labelCup0 = new javax.swing.JLabel();
        labelBid0 = new javax.swing.JLabel();
        labelPlayerCup0 = new javax.swing.JLabel();
        labelPlayerBid0 = new javax.swing.JLabel();
        panelPlayer1 = new javax.swing.JPanel();
        labelCup1 = new javax.swing.JLabel();
        labelBid1 = new javax.swing.JLabel();
        labelPlayerCup1 = new javax.swing.JLabel();
        labelPlayerBid1 = new javax.swing.JLabel();
        panelPlayer2 = new javax.swing.JPanel();
        labelCup2 = new javax.swing.JLabel();
        labelBid2 = new javax.swing.JLabel();
        labelPlayerCup2 = new javax.swing.JLabel();
        labelPlayerBid2 = new javax.swing.JLabel();
        panelPlayer3 = new javax.swing.JPanel();
        labelCup3 = new javax.swing.JLabel();
        labelBid3 = new javax.swing.JLabel();
        labelPlayerCup3 = new javax.swing.JLabel();
        labelPlayerBid3 = new javax.swing.JLabel();
        panelPlayer4 = new javax.swing.JPanel();
        labelCup4 = new javax.swing.JLabel();
        labelBid4 = new javax.swing.JLabel();
        labelPlayerCup4 = new javax.swing.JLabel();
        labelPlayerBid4 = new javax.swing.JLabel();
        panelPlayer5 = new javax.swing.JPanel();
        labelCup5 = new javax.swing.JLabel();
        labelBid5 = new javax.swing.JLabel();
        labelPlayerCup5 = new javax.swing.JLabel();
        labelPlayerBid5 = new javax.swing.JLabel();
        panelPlayer6 = new javax.swing.JPanel();
        labelCup6 = new javax.swing.JLabel();
        labelBid6 = new javax.swing.JLabel();
        labelPlayerCup6 = new javax.swing.JLabel();
        labelPlayerBid6 = new javax.swing.JLabel();
        panelPlayer7 = new javax.swing.JPanel();
        labelCup7 = new javax.swing.JLabel();
        labelBid7 = new javax.swing.JLabel();
        labelPlayerCup7 = new javax.swing.JLabel();
        labelPlayerBid7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Liar's Dice Game Viewer");
        setBackground(java.awt.Color.white);
        setForeground(new java.awt.Color(153, 153, 153));

        buttonNext.setBackground(java.awt.Color.white);
        buttonNext.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        buttonNext.setText("Next");

        buttonExit.setBackground(java.awt.Color.white);
        buttonExit.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        buttonExit.setText("Exit");

        panelStats.setBackground(java.awt.Color.white);
        panelStats.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Stats", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11)));

        labelNumDice.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelNumDice.setText("Total Number of Dice:");

        labelStatsNumDice.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelStatsNumDice.setText("0");

        labelStatsTrashTalk.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelStatsTrashTalk.setForeground(new java.awt.Color(153, 0, 0));
        labelStatsTrashTalk.setText("joefitz");
        labelStatsTrashTalk.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        labelTrashTalk.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelTrashTalk.setText("Trash talk:");

        labelEachDie.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelEachDie.setText("Total of Each Die:");

        labelStatsEachDie.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelStatsEachDie.setText("0");

        org.jdesktop.layout.GroupLayout panelStatsLayout = new org.jdesktop.layout.GroupLayout(panelStats);
        panelStats.setLayout(panelStatsLayout);
        panelStatsLayout.setHorizontalGroup(
            panelStatsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelStatsLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelStatsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(panelStatsLayout.createSequentialGroup()
                            .add(labelNumDice)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(labelStatsNumDice, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(panelStatsLayout.createSequentialGroup()
                            .add(labelEachDie)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(labelStatsEachDie, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
                        .add(panelStatsLayout.createSequentialGroup()
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(labelTrashTalk)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(labelStatsTrashTalk, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        panelStatsLayout.setVerticalGroup(
            panelStatsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelStatsLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelStatsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelNumDice)
                        .add(labelStatsNumDice))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelStatsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelEachDie)
                        .add(labelStatsEachDie))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelStatsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelTrashTalk)
                        .add(labelStatsTrashTalk))
                    .addContainerGap(14, Short.MAX_VALUE))
        );

        labelStatsNumDice.getAccessibleContext().setAccessibleName("labelNumDice");

        panelPlayer0.setBackground(java.awt.Color.white);
        panelPlayer0.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Player", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11)));

        labelCup0.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelCup0.setText("Cup:");

        labelBid0.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelBid0.setText("Bid:");

        labelPlayerCup0.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerCup0.setText("0");

        labelPlayerBid0.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerBid0.setText("0");

        org.jdesktop.layout.GroupLayout panelPlayer0Layout = new org.jdesktop.layout.GroupLayout(panelPlayer0);
        panelPlayer0.setLayout(panelPlayer0Layout);
        panelPlayer0Layout.setHorizontalGroup(
            panelPlayer0Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer0Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(labelCup0)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerCup0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelBid0)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerBid0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap())
        );
        panelPlayer0Layout.setVerticalGroup(
            panelPlayer0Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer0Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelPlayer0Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelCup0)
                        .add(labelBid0)
                        .add(labelPlayerCup0)
                        .add(labelPlayerBid0))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPlayer1.setBackground(java.awt.Color.white);
        panelPlayer1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Player", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11)));

        labelCup1.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelCup1.setText("Cup:");

        labelBid1.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelBid1.setText("Bid:");

        labelPlayerCup1.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerCup1.setText("0");

        labelPlayerBid1.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerBid1.setText("0");

        org.jdesktop.layout.GroupLayout panelPlayer1Layout = new org.jdesktop.layout.GroupLayout(panelPlayer1);
        panelPlayer1.setLayout(panelPlayer1Layout);
        panelPlayer1Layout.setHorizontalGroup(
            panelPlayer1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer1Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(labelCup1)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerCup1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelBid1)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerBid1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap())
        );
        panelPlayer1Layout.setVerticalGroup(
            panelPlayer1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer1Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelPlayer1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelCup1)
                        .add(labelBid1)
                        .add(labelPlayerCup1)
                        .add(labelPlayerBid1))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPlayer2.setBackground(java.awt.Color.white);
        panelPlayer2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Player", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11)));

        labelCup2.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelCup2.setText("Cup:");

        labelBid2.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelBid2.setText("Bid:");

        labelPlayerCup2.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerCup2.setText("0");

        labelPlayerBid2.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerBid2.setText("0");

        org.jdesktop.layout.GroupLayout panelPlayer2Layout = new org.jdesktop.layout.GroupLayout(panelPlayer2);
        panelPlayer2.setLayout(panelPlayer2Layout);
        panelPlayer2Layout.setHorizontalGroup(
            panelPlayer2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer2Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(labelCup2)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerCup2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelBid2)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerBid2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap())
        );
        panelPlayer2Layout.setVerticalGroup(
            panelPlayer2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer2Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelPlayer2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelCup2)
                        .add(labelBid2)
                        .add(labelPlayerCup2)
                        .add(labelPlayerBid2))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPlayer3.setBackground(java.awt.Color.white);
        panelPlayer3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Player", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11)));

        labelCup3.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelCup3.setText("Cup:");

        labelBid3.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelBid3.setText("Bid:");

        labelPlayerCup3.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerCup3.setText("0");

        labelPlayerBid3.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerBid3.setText("0");

        org.jdesktop.layout.GroupLayout panelPlayer3Layout = new org.jdesktop.layout.GroupLayout(panelPlayer3);
        panelPlayer3.setLayout(panelPlayer3Layout);
        panelPlayer3Layout.setHorizontalGroup(
            panelPlayer3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer3Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(labelCup3)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerCup3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelBid3)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerBid3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap())
        );
        panelPlayer3Layout.setVerticalGroup(
            panelPlayer3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer3Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelPlayer3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelCup3)
                        .add(labelBid3)
                        .add(labelPlayerCup3)
                        .add(labelPlayerBid3))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPlayer4.setBackground(java.awt.Color.white);
        panelPlayer4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Player", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11)));

        labelCup4.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelCup4.setText("Cup:");

        labelBid4.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelBid4.setText("Bid:");

        labelPlayerCup4.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerCup4.setText("0");

        labelPlayerBid4.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerBid4.setText("0");

        org.jdesktop.layout.GroupLayout panelPlayer4Layout = new org.jdesktop.layout.GroupLayout(panelPlayer4);
        panelPlayer4.setLayout(panelPlayer4Layout);
        panelPlayer4Layout.setHorizontalGroup(
            panelPlayer4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer4Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(labelCup4)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerCup4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelBid4)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerBid4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap())
        );
        panelPlayer4Layout.setVerticalGroup(
            panelPlayer4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer4Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelPlayer4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelCup4)
                        .add(labelBid4)
                        .add(labelPlayerCup4)
                        .add(labelPlayerBid4))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPlayer5.setBackground(java.awt.Color.white);
        panelPlayer5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Player", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11)));

        labelCup5.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelCup5.setText("Cup:");

        labelBid5.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelBid5.setText("Bid:");

        labelPlayerCup5.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerCup5.setText("0");

        labelPlayerBid5.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerBid5.setText("0");

        org.jdesktop.layout.GroupLayout panelPlayer5Layout = new org.jdesktop.layout.GroupLayout(panelPlayer5);
        panelPlayer5.setLayout(panelPlayer5Layout);
        panelPlayer5Layout.setHorizontalGroup(
            panelPlayer5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer5Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(labelCup5)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerCup5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelBid5)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerBid5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap())
        );
        panelPlayer5Layout.setVerticalGroup(
            panelPlayer5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer5Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelPlayer5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelCup5)
                        .add(labelBid5)
                        .add(labelPlayerCup5)
                        .add(labelPlayerBid5))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPlayer6.setBackground(java.awt.Color.white);
        panelPlayer6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Player", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11)));

        labelCup6.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelCup6.setText("Cup:");

        labelBid6.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelBid6.setText("Bid:");

        labelPlayerCup6.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerCup6.setText("0");

        labelPlayerBid6.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerBid6.setText("0");

        org.jdesktop.layout.GroupLayout panelPlayer6Layout = new org.jdesktop.layout.GroupLayout(panelPlayer6);
        panelPlayer6.setLayout(panelPlayer6Layout);
        panelPlayer6Layout.setHorizontalGroup(
            panelPlayer6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer6Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(labelCup6)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerCup6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelBid6)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerBid6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap())
        );
        panelPlayer6Layout.setVerticalGroup(
            panelPlayer6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer6Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelPlayer6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelCup6)
                        .add(labelBid6)
                        .add(labelPlayerCup6)
                        .add(labelPlayerBid6))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPlayer7.setBackground(java.awt.Color.white);
        panelPlayer7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Player", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11)));

        labelCup7.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelCup7.setText("Cup:");

        labelBid7.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelBid7.setText("Bid:");

        labelPlayerCup7.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerCup7.setText("0");

        labelPlayerBid7.setFont(new java.awt.Font("Comic Sans MS", 0, 11));
        labelPlayerBid7.setText("0");

        org.jdesktop.layout.GroupLayout panelPlayer7Layout = new org.jdesktop.layout.GroupLayout(panelPlayer7);
        panelPlayer7.setLayout(panelPlayer7Layout);
        panelPlayer7Layout.setHorizontalGroup(
            panelPlayer7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer7Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(labelCup7)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerCup7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelBid7)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(labelPlayerBid7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap())
        );
        panelPlayer7Layout.setVerticalGroup(
            panelPlayer7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(panelPlayer7Layout.createSequentialGroup()
                    .addContainerGap()
                    .add(panelPlayer7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(labelCup7)
                        .add(labelBid7)
                        .add(labelPlayerCup7)
                        .add(labelPlayerBid7))
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(panelStats, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(layout.createSequentialGroup()
                            .add(buttonExit)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(buttonNext))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, panelPlayer0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, panelPlayer1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, panelPlayer2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, panelPlayer3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(panelPlayer4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, panelPlayer5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(panelPlayer6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(panelPlayer7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(buttonExit)
                        .add(buttonNext))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelStats, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelPlayer0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelPlayer1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelPlayer2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelPlayer3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelPlayer4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelPlayer5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelPlayer6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(panelPlayer7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(31, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonExit;
    private javax.swing.JButton buttonNext;
    private javax.swing.JLabel labelBid0;
    private javax.swing.JLabel labelBid1;
    private javax.swing.JLabel labelBid2;
    private javax.swing.JLabel labelBid3;
    private javax.swing.JLabel labelBid4;
    private javax.swing.JLabel labelBid5;
    private javax.swing.JLabel labelBid6;
    private javax.swing.JLabel labelBid7;
    private javax.swing.JLabel labelCup0;
    private javax.swing.JLabel labelCup1;
    private javax.swing.JLabel labelCup2;
    private javax.swing.JLabel labelCup3;
    private javax.swing.JLabel labelCup4;
    private javax.swing.JLabel labelCup5;
    private javax.swing.JLabel labelCup6;
    private javax.swing.JLabel labelCup7;
    private javax.swing.JLabel labelPlayerBid0;
    private javax.swing.JLabel labelPlayerBid1;
    private javax.swing.JLabel labelPlayerBid2;
    private javax.swing.JLabel labelPlayerBid3;
    private javax.swing.JLabel labelPlayerBid4;
    private javax.swing.JLabel labelPlayerBid5;
    private javax.swing.JLabel labelPlayerBid6;
    private javax.swing.JLabel labelPlayerBid7;
    private javax.swing.JLabel labelPlayerCup0;
    private javax.swing.JLabel labelPlayerCup1;
    private javax.swing.JLabel labelPlayerCup2;
    private javax.swing.JLabel labelPlayerCup3;
    private javax.swing.JLabel labelPlayerCup4;
    private javax.swing.JLabel labelPlayerCup5;
    private javax.swing.JLabel labelPlayerCup6;
    private javax.swing.JLabel labelPlayerCup7;
    private javax.swing.JLabel labelStatsEachDie;
    private javax.swing.JLabel labelStatsNumDice;
    private javax.swing.JLabel labelStatsTrashTalk;
    private javax.swing.JPanel panelPlayer0;
    private javax.swing.JPanel panelPlayer1;
    private javax.swing.JPanel panelPlayer2;
    private javax.swing.JPanel panelPlayer3;
    private javax.swing.JPanel panelPlayer4;
    private javax.swing.JPanel panelPlayer5;
    private javax.swing.JPanel panelPlayer6;
    private javax.swing.JPanel panelPlayer7;
    // End of variables declaration//GEN-END:variables

    private class PlayerUI
    {
        private JPanel _panelPlayer = null;
        private JLabel _labelCup = null;
        private JLabel _labelPlayerCup = null;
        private JLabel _labelBid = null;
        private JLabel _labelPlayerBid = null;

        PlayerUI(JPanel panelPlayer, JLabel labelCup, JLabel labelPlayerCup, JLabel labelBid, JLabel labelPlayerBid)
        {
            _panelPlayer = panelPlayer;
            _labelCup = labelCup;
            _labelPlayerCup = labelPlayerCup;
            _labelBid = labelBid;
            _labelPlayerBid = labelPlayerBid;
        }

        protected JPanel getPanelPlayer()
        {
            return _panelPlayer;
        }

        protected JLabel getLabelCup()
        {
            return _labelCup;
        }

        protected JLabel getLabelPlayerCup()
        {
            return _labelPlayerCup;
        }

        protected JLabel getLabelBid()
        {
            return _labelBid;
        }

        protected JLabel getLabelPlayerBid()
        {
            return _labelPlayerBid;
        }
    }


}

