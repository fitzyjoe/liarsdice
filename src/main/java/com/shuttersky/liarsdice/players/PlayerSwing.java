package com.shuttersky.liarsdice.players;

import com.shuttersky.liarsdice.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PlayerSwing implements Player, ActionListener
{
    // UI elements
    private JTextArea _textRoundState = null;
    private JLabel _labelTotalDice = null;
    private Bid _swingBid = null;
    private JComboBox _comboBids = null;
    private JButton _btnSubmitBid = null;
    private JLabel _labelCup = null;

    // constructor
    public PlayerSwing()
    {
        // set up GUI to have the native look and feel
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
        }
        _textRoundState = new javax.swing.JTextArea();
        _textRoundState.setFont(new java.awt.Font("Serif", java.awt.Font.PLAIN, 12));
        _textRoundState.setLineWrap(true);
        _textRoundState.setWrapStyleWord(true);
        _textRoundState.setEditable(false);
        JScrollPane areaScrollPane = new JScrollPane(_textRoundState);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(300, 300));
        areaScrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
        _labelTotalDice = new javax.swing.JLabel("");
        _labelTotalDice.setAlignmentX(Component.CENTER_ALIGNMENT);
        _labelCup = new javax.swing.JLabel("");
        _labelCup.setAlignmentX(Component.CENTER_ALIGNMENT);
        _comboBids = new javax.swing.JComboBox();
        _comboBids.setEnabled(false);
        _comboBids.setEditable(false);
        _btnSubmitBid = new javax.swing.JButton("Submit Bid");
        _btnSubmitBid.setActionCommand("submitBid");
        _btnSubmitBid.addActionListener(this);
        _btnSubmitBid.setEnabled(false);

        // add submitbid components to panel
        JPanel panelSubmitBid = new JPanel();
        //panelSubmitBid.setLayout(new BoxLayout(panelSubmitBid, BoxLayout.X_AXIS));
        panelSubmitBid.setLayout(new FlowLayout());
        panelSubmitBid.add(new JLabel("Bid:"));
        panelSubmitBid.add(_comboBids);
        panelSubmitBid.add(_btnSubmitBid);

        // main panel
        JPanel panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));
        panelMain.setBorder(BorderFactory.createEmptyBorder(
            5, //top
            5, //left
            5, //bottom
            5) //right
        );
        panelMain.add(areaScrollPane);
        panelMain.add(_labelTotalDice);
        panelMain.add(_labelCup);
        panelMain.add(panelSubmitBid);

        // put main panel in a component
        Component contents = panelMain;

        // get the icon
        ImageIcon imgIcon = new ImageIcon("dice.gif");

        // create the top most level frame
        JFrame frame = new JFrame("Liar's Dice");
        frame.setIconImage(imgIcon.getImage());

        frame.getContentPane().add(contents, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    public Bid getBid(RoundState rs, Cup cup)
    {
        Bid highestBid = rs.getHighestBid();
        Bid bid = null;
        int iBidQuantity = 0;
        int iBidDots = 0;

        // update the roundstate display
        updateDisplay(rs);

        // Show the cup
        _labelCup.setText("Your Cup: " + cup.toString());

        // set the quantity place holders for new bids
        if (highestBid == null)
        {
            iBidQuantity = 1;
            iBidDots = 1;
        }
        else
        {
            iBidQuantity = highestBid.getNumDice();
            iBidDots = highestBid.getDots() + 1;
            if (iBidDots > Die.DEFAULT_NUM_SIDES)
            {
                iBidDots = 1;
                iBidQuantity++;
            }
        }
        _comboBids.removeAllItems();

        // add a b.s. bid if you're not the first bidder
        if (rs.getNumBids() > 0)
        {
            _comboBids.addItem(new Bid());
        }

        // populate bid combo with valid values
        bid = new Bid();

        while (iBidQuantity <= rs.getNumDiceTotal())
        {
            try
            {
                bid = new Bid(iBidQuantity, iBidDots);
            }
            catch (Exception e)
            {
            }

            _comboBids.addItem(bid);

            iBidDots++;
            if (iBidDots > Die.DEFAULT_NUM_SIDES)
            {
                iBidDots = 1;
                iBidQuantity++;
            }
        }

        // reset the bid variable
        _swingBid = null;

        // enable submit bid controls
        _comboBids.setEnabled(true);
        _btnSubmitBid.setEnabled(true);

        // wait until the user selects a bid
        while (_swingBid == null)
        {
            delay(1);
        }

        return _swingBid;
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("submitBid"))
        {
            handleBtnSubmitBid();
        }
    }

    private void handleBtnSubmitBid()
    {
        // disable submit bid controls
        _comboBids.setEnabled(false);
        _btnSubmitBid.setEnabled(false);

        // set member variable bid
        _swingBid = (Bid) _comboBids.getSelectedItem();
    }

    public void tellBid(RoundState rs)
    {
        updateDisplay(rs);

        delay(1);
    }

    public void tellOutcome(RoundState rs, String sWinnerEmail, String sLoserEmail)
    {
        _textRoundState.append("\n" + sWinnerEmail + " wins the showdown.\n" + sLoserEmail + " loses a die.");
        delay(4);

        // if this helps, store this information to use for future rounds
    }

    private void updateDisplay(RoundState rs)
    {
        _labelTotalDice.setText("Total Dice Left: " + rs.getNumDice());

        _textRoundState.setText(rs.toString());
    }

    private void delay(int iSeconds)
    {
        try
        {
            (Thread.currentThread()).sleep(iSeconds * 1000);
        }
        catch (Exception e)
        {
        }
    }

}
