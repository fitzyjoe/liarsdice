package com.shuttersky.liarsdice.experimental;

import javax.swing.*;

public class GameViewerTester
{
    public static final void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
        }

        try
        {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (Exception e)
        {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        JFrame jFrame = new JFrame("Viewer");
        GameViewer2 gameViewer = new GameViewer2();
        jFrame.setContentPane(gameViewer.gameViewerPanel);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jFrame.pack();
        jFrame.setVisible(true);
    }
}
